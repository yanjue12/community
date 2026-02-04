package com.fzg.controller.app;


import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Commentmapper;
import com.fzg.model.Comment;
import com.fzg.model.Result;
import com.fzg.service.CommentService;
import com.fzg.vo.CommentPageVO;
import com.fzg.vo.CommentVO;
import com.fzg.vo.RootCommentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private Commentmapper commentmapper;
    @Autowired
    private Articlemapper articlemapper;

    @PostMapping("/add")
    public Result add(@RequestBody CommentVO comment){
        if(null == comment || comment.getUserId() == null
        || comment.getArticleId() == null || StringUtils.isEmpty(comment.getContent())){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        //TODO 消息队列
        Boolean b = commentService.saveComment(comment);

        return Result.handle(b);
    }

    /**
     * 获取一级评论
     * @param articleId
     * @param lastId
     * @param size
     * @return
     */
    @GetMapping("/root/page")
    public Result queryRootComList(@RequestParam Long articleId,
                               @RequestParam Long lastId, // 滚动游标
                               @RequestParam Integer size){
        CommentPageVO<RootCommentVO> commentPageVO = commentService.queryComList(articleId,lastId,size);

        return Result.success(commentPageVO);
    }

    @GetMapping("/child/page")
    public Result childPage(Long rootId,@RequestParam(defaultValue = "0") Long lastId,
            @RequestParam(defaultValue = "10") Integer size) {

        return Result.success(commentService.queryChildComList(rootId, lastId, size));
    }


    /**
     * 评论删除 ： 作者可以删除任何评论 非作者只能删自己评论的
     * @param commentVO
     * @return
     */
    @PostMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestBody CommentVO commentVO){
        if(null == commentVO || commentVO.getId() == null){
            throw new RuntimeException("参数为空");
        }
        Comment comment = commentmapper.selectById(commentVO.getId());
        if(null == comment || comment.getStatus().equals("0")){
            throw new RuntimeException("评论不存在或已删除");
        }

        //作者删除评论
        if(commentVO.getAuthorId() == commentVO.getUserId()){
            int i = commentmapper.logicDeleteById(commentVO.getId());
            if(i <= 0){
                throw new RuntimeException("评论删除失败");
            }
            int i1 = articlemapper.decreComCount(commentVO.getArticleId());
            if(i1 <= 0){
                throw new RuntimeException("文章评论数 减1 失败");
            }
        }else{
            //判断删除的是否是自己的评论
            if(comment.getUserId() != commentVO.getUserId()){
                throw new RuntimeException("无权限删除该评论");
            }else {
                //是自己的删
                int i = commentmapper.logicDeleteById(commentVO.getId());
                if(i <= 0){
                    throw new RuntimeException("评论删除失败");
                }
                int i1 = articlemapper.decreComCount(commentVO.getArticleId());
                if(i1 <= 0){
                    throw new RuntimeException("文章评论数 减1 失败");
                }
            }
        }
        if (comment.getRootId() != null && comment.getRootId() > 0) {
            comment.setReplyCount(comment.getReplyCount() - 1);
            int i = commentmapper.updateById(comment);
            if(i <= 0){
                throw new RuntimeException("评论回复数 更新失败");
            }
        }
        return Result.success(true);
    }
}
