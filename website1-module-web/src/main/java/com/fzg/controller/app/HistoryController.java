package com.fzg.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.ArticleViewHistoryMapper;
import com.fzg.mapper.SearchHistoryMapper;
import com.fzg.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 浏览历史 & 搜索历史接口
 */
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@Tag(name = "历史记录", description = "浏览历史与搜索历史管理")
public class HistoryController {

    private final ArticleViewHistoryMapper articleViewHistoryMapper;
    private final SearchHistoryMapper searchHistoryMapper;

    // ==================== 浏览历史 ====================

    /**
     * 查询当前用户最近15条浏览历史，返回文章数据
     */
    @GetMapping("/view/list")
    @Operation(summary = "查询浏览历史（近15条，返回文章信息）")
    public Result getViewHistory() {
        Long userId = getLoginUserId();
        return Result.success(articleViewHistoryMapper.selectTop15ArticlesByUser(userId));
    }

    /**
     * 删除指定浏览历史记录
     * id 用 String 接收，避免前端 JS 精度丢失
     */
    @DeleteMapping("/view/{id}")
    @Operation(summary = "删除指定浏览历史")
    public Result deleteViewHistory(@PathVariable String id) {
        Long userId = getLoginUserId();
        Long recordId = Long.parseLong(id);
        int rows = articleViewHistoryMapper.deleteByIdAndUser(recordId, userId);
        return rows > 0 ? Result.success(true) : Result.fail(EnumReturn.valueOf("记录不存在或无权限"));
    }

    /**
     * 清空当前用户所有浏览历史
     */
    @DeleteMapping("/view/all")
    @Operation(summary = "清空所有浏览历史")
    public Result clearAllViewHistory() {
        Long userId = getLoginUserId();
        articleViewHistoryMapper.deleteAllByUser(userId);
        return Result.success(true);
    }

    // ==================== 搜索历史 ====================

    /**
     * 查询当前用户最近15条搜索历史
     */
    @GetMapping("/search/list")
    @Operation(summary = "查询搜索历史（近15条）")
    public Result getSearchHistory() {
        Long userId = getLoginUserId();
        return Result.success(searchHistoryMapper.selectTop15ByUser(userId));
    }

    /**
     * 删除指定搜索历史记录
     * id 用 String 接收，避免前端 JS 精度丢失
     */
    @DeleteMapping("/search/{id}")
    @Operation(summary = "删除指定搜索历史")
    public Result deleteSearchHistory(@PathVariable String id) {
        Long userId = getLoginUserId();
        Long recordId = Long.parseLong(id);
        int rows = searchHistoryMapper.deleteByIdAndUser(recordId, userId);
        return rows > 0 ? Result.success(true) : Result.fail(EnumReturn.valueOf("记录不存在或无权限"));
    }

    /**
     * 清空当前用户所有搜索历史
     */
    @DeleteMapping("/search/all")
    @Operation(summary = "清空所有搜索历史")
    public Result clearAllSearchHistory() {
        Long userId = getLoginUserId();
        searchHistoryMapper.deleteAllByUser(userId);
        return Result.success(true);
    }

    // ==================== 工具 ====================

    private Long getLoginUserId() {
        return Long.parseLong(StpUtil.getLoginId().toString());
    }
}
