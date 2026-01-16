//package com.fzg.job;
//
//import com.fzg.constant.RedisKeyManager;
//import com.fzg.model.LikeRecord;
//import com.fzg.service.ArticleService;
//import com.fzg.service.LikeRecordService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//
//@Service
//@Slf4j
//public class LikeSyncService {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final LikeRecordService likeRecordService;
//    private final ArticleService articleService;
//
//    // 配置
//    private static final int BATCH_SIZE = 500;
//    private static final int MAX_RETRY = 3;
//
//    @Autowired
//    public LikeSyncService(RedisTemplate<String, String> redisTemplate,
//                          LikeRecordService likeRecordService,
//                          ArticleService articleService) {
//        this.redisTemplate = redisTemplate;
//        this.likeRecordService = likeRecordService;
//        this.articleService = articleService;
//    }
//
//    /**
//     * 同步点赞数到数据库（每分钟执行）
//     */
//    @Scheduled(cron = "0 */1 * * * ?")
//    @Transactional(rollbackFor = Exception.class)
//    public void syncLikeCount() {
//        log.info("开始同步点赞数");
//
//        Set<String> dirtyArticleIds = redisTemplate.opsForSet()
//                .members(RedisKeyManager.getDirtySetKey());
//
//        if (dirtyArticleIds == null || dirtyArticleIds.isEmpty()) {
//            return;
//        }
//
//        List<ArticleLikeUpdate> updates = new ArrayList<>();
//
//        for (String articleIdStr : dirtyArticleIds) {
//            try {
//                Long articleId = Long.parseLong(articleIdStr);
//                String countKey = RedisKeyManager.getArticleLikeCountKey(articleId);
//                String countStr = redisTemplate.opsForValue().get(countKey);
//
//                if (countStr != null) {
//                    ArticleLikeUpdate update = new ArticleLikeUpdate();
//                    update.setArticleId(articleId);
//                    update.setLikeCount(Integer.parseInt(countStr));
//                    updates.add(update);
//                }
//
//                // 从脏数据集中移除
//                redisTemplate.opsForSet().remove(RedisKeyManager.getDirtySetKey(), articleIdStr);
//
//            } catch (Exception e) {
//                log.error("处理文章点赞数失败: {}", articleIdStr, e);
//            }
//        }
//
//        // 批量更新数据库
//        if (!updates.isEmpty()) {
//            batchUpdateArticleLikeCount(updates);
//        }
//
//        log.info("点赞数同步完成，处理{}条记录", updates.size());
//    }
//
//    /**
//     * 同步点赞记录到数据库（每5分钟执行）
//     */
//    @Scheduled(cron = "0 */5 * * * ?")
//    public void syncLikeRecords() {
//        log.info("开始同步点赞记录");
//
//        Set<String> records = redisTemplate.opsForSet()
//                .members(RedisKeyManager.getSyncRecordSetKey());
//
//        if (records == null || records.isEmpty()) {
//            return;
//        }
//
//        List<LikeRecord> likeRecords = new ArrayList<>();
//
//        for (String recordStr : records) {
//            try {
//                // 格式: userId:articleId:action:timestamp
//                String[] parts = recordStr.split(":");
//                if (parts.length >= 4) {
//                    LikeRecord record = new LikeRecord();
//                    record.setUserId(Long.parseLong(parts[0]));
//                    record.setArticleId(Long.parseLong(parts[1]));
//                    record.setStatus(parts[2]);
//                    record.setCreateAt(new Date(Long.parseLong(parts[3])));
//                    likeRecords.add(record);
//                }
//            } catch (Exception e) {
//                log.error("解析点赞记录失败: {}", recordStr, e);
//            }
//        }
//
//        // 批量保存到数据库
//        if (!likeRecords.isEmpty()) {
//            likeRecordService.batchSaveRecords(likeRecords);
//
//            // 清理已同步的记录
//            records.forEach(record ->
//                redisTemplate.opsForSet().remove(RedisKeyManager.getSyncRecordSetKey(), record)
//            );
//        }
//
//        log.info("点赞记录同步完成，处理{}条记录", likeRecords.size());
//    }
//
//    /**
//     * 批量更新文章点赞数（带重试机制）
//     */
//    @Retryable(value = Exception.class, maxAttempts = MAX_RETRY)
//    private void batchUpdateArticleLikeCount(List<ArticleLikeUpdate> updates) {
//        // 分批处理
//        List<List<ArticleLikeUpdate>> partitions = new ArrayList<>();
//        for (int i = 0; i < updates.size(); i += BATCH_SIZE) {
//            partitions.add(updates.subList(i,
//                Math.min(i + BATCH_SIZE, updates.size())));
//        }
//
//        for (List<ArticleLikeUpdate> batch : partitions) {
//            articleService.batchUpdateLikeCount(batch);
//        }
//    }
//
//    /**
//     * 修复数据不一致问题（每天凌晨执行）
//     */
//    @Scheduled(cron = "0 0 3 * * ?")
//    public void fixDataInconsistency() {
//        log.info("开始修复点赞数据不一致问题");
//
//        // 1. 对比Redis和MySQL的点赞数
//        // 2. 记录差异并修复
//        // 3. 清理过期的Redis key
//
//        log.info("数据修复完成");
//    }
//}
//
///**
// * 点赞数更新DTO
// */
//@Data
//class ArticleLikeUpdate {
//    private Long articleId;
//    private Integer likeCount;
//}