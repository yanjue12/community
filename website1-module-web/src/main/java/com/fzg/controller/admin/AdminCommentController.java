package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.Commentmapper;
import com.fzg.model.Comment;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-评论管理
 */
@RestController
@RequestMapping("/admin/comment")
@SaCheckLogin
public class AdminCommentController {

    @Autowired
    private Commentmapper commentMapper;

    /**
     * 分页查询评论列表
     */
    @GetMapping("/list")
    public Result listComments(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) Long articleId,
                              @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) Integer status) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (userId != null) {
            wrapper.eq(Comment::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreatedAt);
        return Result.success(commentMapper.selectPage(page, wrapper));
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public Result getComment(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        return comment != null ? Result.success(comment) : Result.fail(404, "评论不存在");
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        int result = commentMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody java.util.List<Long> ids) {
        int result = commentMapper.deleteBatchIds(ids);
        return Result.handle(result > 0);
    }


    /**
     * 置顶评论
     */
    @PutMapping("/{id}/top")
    public Result topComment(@PathVariable Long id, @RequestParam String isTop) {
        Comment comment = new Comment();
        comment.setId(id);
        int result = commentMapper.updateById(comment);
        return Result.handle(result > 0);
    }

}
