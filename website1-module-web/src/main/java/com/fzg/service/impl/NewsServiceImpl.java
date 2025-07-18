package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.bo.NewsCreateBO;
import com.fzg.config.MinioProperties;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.NewsDetailMapper;
import com.fzg.model.News;
import com.fzg.model.NewsDetail;
import com.fzg.model.Result;
import com.fzg.service.MinioService;
import com.fzg.service.NewsService;
import com.fzg.mapper.NewsMapper;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author yanju
* @description 针对表【news(新闻表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

    private final NewsMapper newsMapper;

    private final MinioService minioService;

    private final MinioClient minioClient;

    private final NewsDetailMapper newsDetailsMapper;

    private final MinioProperties minioProperties;

    private final ThreadLocal<List<String>> uploadedImages = ThreadLocal.withInitial(ArrayList::new);

    // 正则表达式匹配临时图片URL
    private static final Pattern TEMP_IMG_PATTERN =
            Pattern.compile("src=\"(.*?/temp-images/.*?)\"");




    /**
     * 获取新闻详情
     * @return
     */
    @Override
    public Result newsList() {

        //获取新闻列表
        List<News> list = this.list();
        if(list.isEmpty()){
            return Result.fail(EnumReturn.NEWS_LIST_EMPTY);
        }
        List<NewsCreateBO> newsCreateBOList = new ArrayList<>();

        for (News news : list) {
            NewsCreateBO newsCreateBO = new NewsCreateBO();
            newsCreateBO.setId(news.getId());
            newsCreateBO.setTitle(news.getTitle());
            newsCreateBO.setSummary(news.getSummary());
            newsCreateBO.setLabel(news.getLabel());
            newsCreateBO.setPublishDate(news.getPublishDate());
            newsCreateBOList.add(newsCreateBO);
        }

        List<Integer> ids = list.stream().map(News::getId).collect(Collectors.toList());

        //批量 详情
        List<NewsDetail> details = getNewsDetailsByNewIds(ids);
        //log.info("根据新闻id列表查询新闻详情，ids：{}，详情：{}",ids,details);

        // 将详情存储在 Map 中以便快速查找
        Map<Integer, String> detailsMap = details.stream()
                .collect(Collectors.toMap(NewsDetail::getNewsId, NewsDetail::getContent));

        //直接封装，会一一对应，在保存新闻时添加了必存在的逻辑
        // 设置内容到对应的 NewsCreateBO 中
        for (NewsCreateBO newsCreateBO : newsCreateBOList) {
            String content = detailsMap.get(newsCreateBO.getId());
            newsCreateBO.setContent(content);
        }

        //log.info("获取新闻详情，新闻列表：{}",newsCreateBOList);
        return Result.success(newsCreateBOList);
    }

    /**
     * 根据新闻id列表查询新闻详情
     * @param ids
     * @return
     */
    private List<NewsDetail> getNewsDetailsByNewIds(List<Integer> ids) {
        LambdaQueryWrapper<NewsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(NewsDetail::getNewsId,ids);
        return newsDetailsMapper.selectList(queryWrapper);
    }


    /**
     * 创建新闻
     * @param newsCreateBO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createNewsWithContent(NewsCreateBO newsCreateBO) {
        //1.保存新闻主记录
        News news = new News();
        //news.setId(newsCreateBO.getId());
        news.setTitle(newsCreateBO.getTitle());
        news.setSummary(newsCreateBO.getSummary());
        news.setLabel(newsCreateBO.getLabel());
        news.setPublishDate(newsCreateBO.getPublishDate());
        newsMapper.insert(news);

        Integer newsId = news.getId();
       // log.debug("插入数据库后使用mp自动回填 主键id:{}", newsId);

        //处理 details 字符串
        String content = newsCreateBO.getContent();
        Document document = Jsoup.parse(content);
        Elements imgElements = document.select("src");

        for(Element imgElement : imgElements){
            String imgUrl = imgElement.attr("src");

            //上传获取新url
            String Url = minioService.uploadByUrl(imgUrl, minioProperties.getBucketName(), minioClient);

            log.debug("上传图片，旧url：{}，新url：{}",imgUrl,Url);

            //替换url
            imgElement.attr("src",Url);
        }

        String updatedContent = document.body().html();


        NewsDetail newsDetail = new NewsDetail();
        newsDetail.setContent(updatedContent);
        newsDetail.setNewsId(newsId);
        newsDetailsMapper.insert(newsDetail);

        return Result.success(updatedContent);

        /*// 注册事务回调
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteUploadedImages();
                }
            }
        });

        //1.保存新闻主记录
        News news = new News();
        //news.setId(newsCreateBO.getId());
        news.setTitle(newsCreateBO.getTitle());
        news.setSummary(newsCreateBO.getSummary());
        news.setLabel(newsCreateBO.getLabel());
        //
        news.setPublishDate(newsCreateBO.getPublishDate());
        newsMapper.insert(news);

        Integer newsId = news.getId();
        log.debug("插入数据库后使用mp自动回填 主键id:{}", newsId);

        //处理富文本里的图片
        String processedContent = processImagesInContent(newsCreateBO.getContent(),news.getId());

        NewsDetail newsDetail = new NewsDetail();
        newsDetail.setContent(processedContent);
        newsDetail.setNewsId(newsId);
        newsDetailsMapper.insert(newsDetail);*/
    }



    /**
     * 删除新闻
     * @param id 新闻id
     */
    @Override
    public void deleteNews(Integer id) {
        //不逻辑删了，直接删
        LambdaQueryWrapper<NewsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NewsDetail::getNewsId, id);
        newsDetailsMapper.deleteById(id);
        this.removeById(id);

    }



    /**
     * 更新新闻
     * @param id 新闻id
     * @param newsCreateBO 新闻更新信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNews(Integer id, NewsCreateBO newsCreateBO) {
        // 根据新闻 ID 查询新闻
        News news = this.getById(id);
        if (news == null) {
            throw new RuntimeException(EnumReturn.NEWS_NOT_EXIST.getDesc());
        }

        news.setTitle(newsCreateBO.getTitle());
        news.setSummary(newsCreateBO.getSummary());
        news.setLabel(newsCreateBO.getLabel());
        news.setPublishDate(newsCreateBO.getPublishDate());
        this.updateById(news);

        //TODO 处理富文本里的图片

        // 查询新闻详情
        LambdaQueryWrapper<NewsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NewsDetail::getNewsId, id);
        NewsDetail newsDetail = newsDetailsMapper.selectOne(queryWrapper);
        String newContent = newsCreateBO.getContent();
        String oldContent = newsDetail.getContent();


        if(newsDetail != null && Objects.equals(oldContent, newContent)){
            return;
        }


        Document newDoc = Jsoup.parse(newContent);
        Elements newImgElements = newDoc.select("img");
        Map<String, String> oldImgUrls = new HashMap<>();


        Document oldDoc = Jsoup.parse(newsDetail.getContent());
        Elements oldImgElements = oldDoc.select("img");
        oldImgElements.forEach(img -> oldImgUrls.put(img.attr("src"), img.attr("src")));


        for (Element imgElement : newImgElements) {
            String imgUrl = imgElement.attr("src");
            if (!oldImgUrls.containsKey(imgUrl)) {
                // 图片 URL 变化，上传到 MinIO 并替换
                String newUrl = minioService.uploadByUrl(imgUrl, minioProperties.getBucketName(), minioClient);
                log.debug("上传图片，旧 url：{}，新 url：{}", imgUrl, newUrl);
                imgElement.attr("src", newUrl);
            }
        }

        String updatedContent = newDoc.body().html();

        if (newsDetail == null) {
            // 若新闻详情不存在，则创建新的新闻详情记录
            newsDetail = new NewsDetail();
            newsDetail.setNewsId(id);
            newsDetail.setContent(updatedContent);
            newsDetailsMapper.insert(newsDetail);
        } else {
            // 若新闻详情存在，则更新新闻详情内容
            newsDetail.setContent(updatedContent);
            newsDetailsMapper.updateById(newsDetail);
        }


       /* if (newsDetail == null) {
            // 若新闻详情不存在，则创建新的新闻详情记录
            newsDetail = new NewsDetail();
            newsDetail.setNewsId(id);
            //TODO 新闻详情的内容填充
            //newsDetail.setContent();
            newsDetailsMapper.insert(newsDetail);
        } else {
            // 若新闻详情存在，则更新新闻详情内容
            newsDetail.setContent(newContent);
            newsDetailsMapper.updateById(newsDetail);
        }*/


    }


}




