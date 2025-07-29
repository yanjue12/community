package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.bo.NewsCreateBO;
import com.fzg.config.MinioProperties;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.NewsDetailMapper;
import com.fzg.model.News;
import com.fzg.model.NewsDetail;
import com.fzg.model.Result;
import com.fzg.service.NewsService;
import com.fzg.mapper.NewsMapper;
import com.fzg.vo.NewsVO;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    private final MinioServiceImpl minioService;

    private final MinioClient minioClient;

    private final NewsDetailMapper newsDetailsMapper;

    private final MinioProperties minioProperties;


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
        String content = newsCreateBO.getContent();
        if(content == null){
            return Result.fail(EnumReturn.NEWS_CONTENT_EMPTY);
        }

        NewsDetail newsDetail = new NewsDetail();
        newsDetail.setContent(content);
        newsDetail.setNewsId(newsId);
        newsDetailsMapper.insert(newsDetail);

        return Result.success(content);

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
            log.debug("更新新闻失败，新闻ID：{}不存在",id);
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
            log.debug("新闻内容未改变，无需更新,此时的数据库新闻：{},前端传的新闻：{}", oldContent, newContent);
            return;
        }


        // 获取新内容中的所有图片URL
        Document newDoc = Jsoup.parse(newContent);
        Elements newImgElements = newDoc.select("img");
        Set<String> newImgUrls = newImgElements.stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toSet());

        // 获取旧内容中的所有图片URL
        Document oldDoc = Jsoup.parse(oldContent);
        Elements oldImgElements = oldDoc.select("img");
        Set<String> oldImgUrls = oldImgElements.stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toSet());

        // 计算需要删除的图片：在旧内容中存在但新内容中不存在的图片
        Set<String> imagesToDelete = new HashSet<>(oldImgUrls);
        imagesToDelete.removeAll(newImgUrls); // 差集：保留旧图片中不被新内容使用的部分

        // 删除不再使用的旧图片
        for (String imgUrl : imagesToDelete) {
            log.info("开始删除废弃图片，URL: {}", imgUrl);
            minioService.removeFile(imgUrl, minioProperties.getBucketName(), minioClient);
            log.info("删除废弃图片成功，URL: {}", imgUrl);
        }
        String updatedContent = newDoc.body().html();

        if (newsDetail == null) {
            newsDetail = new NewsDetail();
            newsDetail.setNewsId(id);
            newsDetail.setContent(updatedContent);
            newsDetailsMapper.insert(newsDetail);
        } else {
            newsDetail.setContent(updatedContent);
            newsDetailsMapper.updateById(newsDetail);
        }

    }



    @Override
    public Result<Page<NewsVO>> userNewsList(Integer pageNumber, Integer pageSize) {
        Page<News> page = new Page<>(pageNumber, pageSize);
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getStates, 1); // 只显示已发布新闻

        long count = this.count();
        count = (long) Math.ceil((double) count / pageSize);

        Page<News> newsPage = this.page(page, queryWrapper);
        List<News> newsList = newsPage.getRecords();
        Page<NewsVO> newsListPage = new Page<>(pageNumber, pageSize, page.getTotal());
        List<NewsVO> newsVOList = new ArrayList<>();
        log.info("count:{},total:{}",count,page.getTotal());


        for (News news : newsList) {
            NewsVO newsVO = new NewsVO();
            newsVO.setTitle(news.getTitle());
            newsVO.setSummary(news.getSummary());
            newsVO.setLabel(news.getLabel());
            newsVO.setPublishDate(news.getPublishDate());
            newsVO.setId(news.getId());

            newsVOList.add(newsVO);
        }

        newsListPage.setRecords(newsVOList);
        newsListPage.setTotal(count);

        return Result.success(newsListPage);

    }


    @Override
    public Result<List<NewsCreateBO>> adminNewsList() {

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

        List<NewsDetail> details = getNewsDetailsByNewIds(ids);

        Map<Integer, String> detailsMap = details.stream()
                .collect(Collectors.toMap(NewsDetail::getNewsId, NewsDetail::getContent));

        //直接封装，会一一对应，在保存新闻时添加了必存在的逻辑
        // 设置内容到对应的 NewsCreateBO 中
        for (NewsCreateBO newsCreateBO : newsCreateBOList) {
            String content = detailsMap.get(newsCreateBO.getId());
            newsCreateBO.setContent(content);
        }

        return Result.success(newsCreateBOList);

    }


}




