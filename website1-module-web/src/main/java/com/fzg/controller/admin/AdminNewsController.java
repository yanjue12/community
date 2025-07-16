package com.fzg.controller.admin;

import com.fzg.bo.NewsCreateBO;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.NewsService;
import com.fzg.vo.NewsDetailsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/news")
@Schema(description = "管理员新闻管理接口")
@RequiredArgsConstructor
@Slf4j
public class AdminNewsController {

    private final NewsService newsService;

    @PostMapping("/add")
    public Result createNews(@RequestBody NewsCreateBO newsCreateBO) {
        try {
            // 调用服务层方法创建新闻
            return Result.success( newsService.createNewsWithContent(newsCreateBO));
        } catch (Exception e) {
            // 处理异常
            return Result.fail(EnumReturn.NEWS_CREATE_ERROR);
        }
    }
}
