package com.fzg.controller.app;

import com.fzg.model.Result;
import com.fzg.service.NewsDetailService;
import com.fzg.vo.NewsDetailsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/newsDetails")
@Schema(description = "新闻详情接口")
@RequiredArgsConstructor
@Slf4j
public class NewsDetailsController {

    private final NewsDetailService newsDetailService;

    /**
     * 获取新闻详情
     * @return
     */
    @GetMapping("/list/{id}")
    public Result<NewsDetailsVO> list(@PathVariable("id") Integer id) {


        return newsDetailService.selectByNewsId(id);
    }
}
