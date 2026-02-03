package com.fzg.controller.app;


import com.fzg.enums.EnumReturn;
import com.fzg.model.Comment;
import com.fzg.model.Result;
import com.fzg.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

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


    @PostMapping("/delete")
    public Result delete(@RequestBody Comment comment){
        return Result.success("删除成功");
    }
}
