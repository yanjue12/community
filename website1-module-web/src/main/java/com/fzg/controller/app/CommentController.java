package com.fzg.controller.app;


import com.fzg.enums.EnumReturn;
import com.fzg.model.Comment;
import com.fzg.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment){
        if(null == comment || comment.getUserId() == null
        || comment.getArticleId() == null || StringUtils.isEmpty(comment.getContent())){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }



        return Result.success("添加成功");
    }


    @PostMapping("/delete")
    public Result delete(@RequestBody Comment comment){
        return Result.success("删除成功");
    }
}
