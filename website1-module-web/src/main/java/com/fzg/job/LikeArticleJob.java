package com.fzg.job;

import com.fzg.constant.RedisArticleKey;
import com.fzg.mapper.Articlemapper;
import com.fzg.vo.ArticleLikeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LikeArticleJob {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Articlemapper articleMapper;

    @Scheduled(cron = "0 */1 * * * ?") // 每分钟
    @Transactional
    public void syncArticleLikeCountBatch() {
        log.info("开始执行点赞数同步任务");

        final int REDIS_POP_SIZE = 1000;   // 每次从 dirty set 取多少
        final int DB_BATCH_SIZE = 500;     // 每批 SQL 更新多少

        List<ArticleLikeVO> allList = new ArrayList<>();

        // 1️⃣ 分批 pop dirty set（O(1)，不会阻塞 Redis）
        while (true) {
            List<String> batchIds = redisTemplate.opsForSet()
                    .pop(RedisArticleKey.DIRTY_SET, REDIS_POP_SIZE);

            if (batchIds == null || batchIds.isEmpty()) {
                break;
            }

            for (String idStr : batchIds) {
                Long articleId = Long.valueOf(idStr);
                String countKey = RedisArticleKey.getLikeArticleCountKey(articleId);
                String redisCount = (String) redisTemplate.opsForValue().get(countKey);

                // Redis 过期 / 丢失，跳过（DB 仍然是最终值）
                if (redisCount == null) {
                    continue;
                }

                ArticleLikeVO vo = new ArticleLikeVO();
                vo.setArticleId(articleId);
                vo.setLikeCount(Integer.parseInt(redisCount));
                allList.add(vo);
            }
        }

        if (allList.isEmpty()) {
            return;
        }

        //DB 分批更新（避免超大 SQL / 长事务）
        List<List<ArticleLikeVO>> partitions = partition(allList, DB_BATCH_SIZE);
        for (List<ArticleLikeVO> part : partitions) {
            articleMapper.batchUpdateLikeCount(part);
        }

        //DB 成功后，安全删除 Redis 点赞缓存
        List<String> deleteKeys = allList.stream()
                .map(vo -> RedisArticleKey.getLikeArticleCountKey(vo.getArticleId()))
                .collect(Collectors.toList());

        redisTemplate.delete(deleteKeys);
        log.info("点赞数同步任务执行完毕");
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
