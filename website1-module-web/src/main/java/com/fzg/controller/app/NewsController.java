package com.fzg.controller.app;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/news")
@Schema(description = "新闻接口")
public class NewsController {
}
