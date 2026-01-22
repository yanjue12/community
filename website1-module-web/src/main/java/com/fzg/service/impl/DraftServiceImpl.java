package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Categorymapper;
import com.fzg.mapper.DraftMapper;
import com.fzg.model.Category;
import com.fzg.model.Draft;
import com.fzg.model.Result;
import com.fzg.service.DraftService;
import com.fzg.vo.ArticleRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DraftServiceImpl extends ServiceImpl<DraftMapper, Draft> implements DraftService {

    @Autowired
    private Categorymapper categorymapper;

    @Override
    public Result saveArticleDraft(ArticleRequest articleRequest) {
        try {
            Draft draft = new Draft();
            draft.setUserId(articleRequest.getUserId());
            draft.setTitle(articleRequest.getTitle());
            draft.setSummary(articleRequest.getSummary());
            draft.setContent(articleRequest.getContent());
            draft.setCoverImage(articleRequest.getCoverImage());
            String categoryName = articleRequest.getCategoryName();
            if(StringUtils.isEmpty(categoryName)){
                draft.setCategoryId(0L);
            }else{
                LambdaQueryWrapper<Category> q = new LambdaQueryWrapper<>();
                q.eq(Category::getName, categoryName);
                Category category = categorymapper.selectOne(q);
                draft.setCategoryId(category.getId());
            }
            draft.setTags(articleRequest.getTag());
            draft.setVisibility("1");//私密

            if(null == articleRequest.getDraftId()){
                baseMapper.insert(draft);
            } else {
                draft.setId(articleRequest.getDraftId());
                baseMapper.updateById(draft);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.success("保存成功");
    }
}
