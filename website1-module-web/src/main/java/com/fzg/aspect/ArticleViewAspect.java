package com.fzg.aspect;

import com.fzg.mapper.ArticleViewHistoryMapper;
import com.fzg.model.ArticleViewHistory;
import com.fzg.service.ArticleStatService;
import com.fzg.vo.ArticleDetailVO;
import com.fzg.vo.ArticleRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class ArticleViewAspect {

    @Autowired
    private ArticleStatService articleStatService;

    @Autowired
    private ArticleViewHistoryMapper articleViewHistoryMapper;

    @Autowired(required = false)
    private HttpServletRequest httpServletRequest;

    @AfterReturning(value = "@annotation(com.fzg.annotation.ArticleViewTrack)",
            returning = "result")
    public void trackView(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        ArticleRequest req = (ArticleRequest) args[0];

        // 统计浏览量（原有逻辑）- 需要从Result中取出ArticleDetailVO
        if (result instanceof com.fzg.model.Result) {
            com.fzg.model.Result<?> r = (com.fzg.model.Result<?>) result;
            if (r.getCode() == 200 && r.getData() instanceof ArticleDetailVO) {
                ArticleDetailVO article = (ArticleDetailVO) r.getData();
                articleStatService.handleViewCount(
                        req.getArticleId(),
                        req.getUserId(),
                        req.getIp(),
                        article.getAuthorId()
                );
            }
        }

        // 异步写入浏览历史
        saveViewHistoryAsync(req);
    }

    @Async
    public void saveViewHistoryAsync(ArticleRequest req) {
        try {
            Long userId = req.getUserId();
            Long articleId = req.getArticleId();

            // 未登录用户不记录历史
            if (userId == null || articleId == null) return;

            // 已有记录则只更新时间，否则新增
            int updated = articleViewHistoryMapper.updateViewTime(userId, articleId);
            if (updated > 0) return;

            ArticleViewHistory history = new ArticleViewHistory();
            history.setArticleId(articleId);
            history.setUserId(userId);
            history.setViewerIp(req.getIp());
            history.setViewDuration(0);
            history.setCreatedAt(new Date());

            if (httpServletRequest != null) {
                String ua = httpServletRequest.getHeader("User-Agent");
                if (ua != null && ua.length() > 500) {
                    ua = ua.substring(0, 500);
                }
                history.setViewerUserAgent(ua);
            }

            articleViewHistoryMapper.insert(history);
        } catch (Exception e) {
            log.warn("写入浏览历史失败: articleId={}, userId={}, err={}", req.getArticleId(), req.getUserId(), e.getMessage());
        }
    }
}
