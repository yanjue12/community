package com.fzg.aspect;

import com.fzg.service.ArticleStatService;
import com.fzg.vo.ArticleDetailVO;
import com.fzg.vo.ArticleRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ArticleViewAspect {

    @Autowired
    private ArticleStatService articleStatService;

    @AfterReturning(value = "@annotation(com.fzg.annotation.ArticleViewTrack)",
            returning = "article")
    public void trackView(JoinPoint joinPoint, ArticleDetailVO article){
        System.out.println("开始执行切面逻辑");
        Object[] args = joinPoint.getArgs();
        ArticleRequest req = (ArticleRequest) args[0];


        articleStatService.handleViewCount(
                req.getArticleId(),
                req.getUserId(),
                req.getIp(),
                article.getAuthorId()
        );

    }
}
