package com.fzg.controller.app;

import com.fzg.model.Result;
import com.fzg.service.NewsService;
import com.fzg.vo.NewsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app/news")
@Schema(description = "新闻接口")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final NewsService newsService;


    @GetMapping("/list")
    public Result<List<NewsVO>> list(){

        return newsService.newsList();


       // return Result.success(newsVOList);
    }
}
