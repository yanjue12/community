package com.fzg.controller.app;


import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Commentmapper;
import com.fzg.model.Comment;
import com.fzg.model.Result;
import com.fzg.service.CommentService;
import com.fzg.vo.CommentPageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private Commentmapper commentmapper;

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment){
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
        CommentPageVO commentPageVO = commentService.queryComList(articleId,lastId,size);

        return Result.success(commentPageVO);
    }

    @GetMapping("/child/page")
    public Result childPage(Long rootId,@RequestParam(defaultValue = "0") Long lastId,
            @RequestParam(defaultValue = "10") Integer size) {

        return Result.success(commentService.queryChildComList(rootId, lastId, size));
    }


    @PostMapping("/delete")
    public Result delete(@RequestBody Comment comment){
        return Result.success("删除成功");
    }
}
