package com.fzg.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.DraftMapper;
import com.fzg.model.Draft;
import com.fzg.model.Result;
import com.fzg.service.DraftService;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.DraftVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/draft")
@RestController
public class DraftController {


    @Autowired
    private DraftService draftService;
    @Autowired
    private DraftMapper draftMapper;


    @PostMapping("/saveArticleDraft")
    public Result saveArticleDraft(@RequestBody ArticleRequest articleRequest) {
        if (null == articleRequest) {
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if (null == articleRequest.getUserId()) {
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        return draftService.saveArticleDraft(articleRequest);
    }



    @PostMapping("/deleteArticleDraft")
    public boolean deleteArticleDraft(@RequestBody ArticleRequest articleRequest) {

        return draftService.removeById(articleRequest.getDraftId());
    }

    @PostMapping("/queryArticleDraft")
    public Result queryArticleDraft(@RequestBody ArticleRequest articleRequest) {
        if (null == articleRequest) {
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if (null == articleRequest.getUserId()) {
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        List<DraftVO> drafts = draftService.queryArticleDraft(articleRequest.getUserId(), pageNum, pageSize);

        return Result.success(drafts);
    }
}
