package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.LikeRecordMapper;
import com.fzg.model.LikeRecord;
import com.fzg.model.Result;
import com.fzg.service.LikeRecordService;
import com.fzg.vo.LikeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {
    @Override
    public Result articleLike(LikeRequest likeRequest) {
        Integer actionLike = likeRequest.getActionLike();
        Long userId = likeRequest.getUserId();
        Long articleId = likeRequest.getArticleId();

        try{
            // 1️ 查当前是否已点赞
            LikeRecord record = getOne(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, userId)
                            .eq(LikeRecord::getArticleId, articleId)
                            .eq(LikeRecord::getArticleType, likeRequest.getType())
            );
        if(actionLike == 1){
            if(record != null){
                return Result.success( true);
            }
            //表示当前状态是已点赞，取消点赞，直接删除点赞表里的数 据
            LikeRecord newRecord = new LikeRecord();
            newRecord.setUserId(userId);
            newRecord.setArticleId(articleId);
            newRecord.setArticleType(likeRequest.getType());
            save(newRecord);
            // TODO 文章 喜欢数加1
            return Result.success(true);
        }
        if(actionLike == 0){
            if (record == null) {
                // 本来就没点赞，直接成功
                return Result.success(true);
            }
            removeById(record.getId());

            // TODO：文章 like_count -1
            return Result.success(true);
        }

        }
        catch (Exception e){
            log.info("点赞失败:{}", e);
            return Result.fail(EnumReturn.OPERATION_FAIL);
        }
        return Result.fail(EnumReturn.OPERATION_FAIL);
    }
}
