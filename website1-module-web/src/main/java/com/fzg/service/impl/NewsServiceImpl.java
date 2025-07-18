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
import com.fzg.service.NewsService;
import com.fzg.mapper.NewsMapper;
import com.fzg.vo.NewsVO;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    private final MinioClient minioClient;

    private final NewsDetailMapper newsDetailsMapper;

    private final MinioProperties minioProperties;

    private final ThreadLocal<List<String>> uploadedImages = ThreadLocal.withInitial(ArrayList::new);

    // 正则表达式匹配临时图片URL
    private static final Pattern TEMP_IMG_PATTERN =
            Pattern.compile("src=\"(.*?/temp-images/.*?)\"");




    /**
     * 获取新闻列表
     * @return
     */
    @Override
    public Result<List<NewsVO>> newsList() {

        //状态正常
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getStates, 1);
        List<News> newsList = newsMapper.selectList(queryWrapper);

        log.info("新闻列表的数量list:{}", newsList.size());
        if(newsList.isEmpty()){
            return Result.fail(EnumReturn.NEWS_NOT_EXIST);
        }
        List<NewsVO> newsVOList = new ArrayList<>();
        for(News news: newsList){
            NewsVO newsVO = new NewsVO();
            //fixme 这里直接copy可能会出现问题导致数据丢失，需要手动赋值
            BeanUtils.copyProperties(news,newsVO);
            newsVOList.add(newsVO);
        }
        log.info("新闻列表vo数据newsVOList:{}", newsVOList);
        return Result.success(newsVOList);
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
        // fixme 这里可能不需要设置发布时间
        news.setPublishDate(newsCreateBO.getPublishDate());
        newsMapper.insert(news);

        Integer newsId = news.getId();
        log.debug("插入数据库后使用mp自动回填 主键id:{}", newsId);

        // 2. 替换所有图片地址为占位符
        String processedContent = replaceImageSrcWithPlaceholder(newsCreateBO.getContent());

        NewsDetail newsDetail = new NewsDetail();
        newsDetail.setContent(processedContent);
        newsDetail.setNewsId(newsId);
        newsDetailsMapper.insert(newsDetail);

        return Result.success(processedContent);

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
        // fixme 这里可能不需要设置发布时间
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

        if (newsDetail == null) {
            // 若新闻详情不存在，则创建新的新闻详情记录
            newsDetail = new NewsDetail();
            newsDetail.setNewsId(id);
            //TODO 新闻详情的内容填充
            //newsDetail.setContent();
            newsDetailsMapper.insert(newsDetail);
        } else {
            // 若新闻详情存在，则更新新闻详情内容
            //newsDetail.setContent();
            newsDetailsMapper.updateById(newsDetail);
        }


    }




    /**
     * 替换富文本中所有图片src为占位符
     * @param content 富文本内容
     * @return 替换后的内容
     */
    private String replaceImageSrcWithPlaceholder(String content) {
        // 使用Jsoup解析HTML
        Document doc = Jsoup.parse(content);

        // 选择所有img标签
        Elements imgElements = doc.select("img");
        log.info("img标签的数量:{}", imgElements.size());

        // 遍历所有图片元素
        for (Element img : imgElements) {
            // 将src属性替换为占位符
            img.attr("src", "${testImg}");

            // 可选：添加自定义属性保存原始URL（便于后续处理）
           /* String originalSrc = img.attr("src");
            img.attr("data-original-src", originalSrc);*/
        }

        // 返回处理后的HTML
        return doc.html();
    }




    /**
     * 处理富文本里的图片
     * @param content 富文本内容
     * @param id 新闻id
     * @return String 处理后的富文本内容
     */
   /* public String processImagesInContent(@NotBlank(message = "新闻内容不能为空") String content, Integer id) {

        Matcher matcher = TEMP_IMG_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String imgSrc = matcher.group(1);
            if(imgSrc.matches("^data:image/(png|jpeg|jpg|gif);base64,.*")){
                try {
                    // 提取图片数据
                    String base64Data = imgSrc.substring(imgSrc.indexOf(",") + 1);
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

                    InputStream inputStream = new ByteArrayInputStream(imageBytes);

                    // 生成唯一的图片名称
                    String imageName = "news/" + id + "/" + UUID.randomUUID() + ".png";

                    // 上传图片到 MinIO
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(minioProperties.getBucketName())
                                    .object(imageName)
                                    .stream(inputStream, inputStream.available(), -1)
                                    .contentType("image/png")
                                    .build());

                    // 生成新的图片 URL
                    String newImgUrl = minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + imageName;

                    // 替换图片地址
                    matcher.appendReplacement(sb, "<img src=\"" + newImgUrl + "\"");
                } catch (Exception e) {
                    log.error("图片上传失败: {}", e.getMessage());
                    // 保留原始图片地址
                    matcher.appendReplacement(sb, matcher.group(0));
                }
            }else {
                // 非 base64 图片，保留原始地址
                matcher.appendReplacement(sb, matcher.group(0));
            }

        }
        matcher.appendTail(sb);
        return sb.toString();
    }*/


}




