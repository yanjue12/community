package com.fzg.essync;

import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手动触发 MySQL --> ES 数据同步
 */
@RestController
@RequestMapping("/admin/es")
public class EsSyncController {

    @Autowired
    private ArticleEsSyncService syncService;

    @PostMapping("/syncArticles")
    public Result syncArticles() {
        syncService.fullSyncToEs();
        return Result.success("ES 同步完成");
    }
}
