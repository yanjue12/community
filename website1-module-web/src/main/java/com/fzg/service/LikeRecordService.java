package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.LikeRecord;
import com.fzg.model.Result;
import com.fzg.vo.LikeRequest;


public interface LikeRecordService extends IService<LikeRecord> {
    Result articleLike(LikeRequest likeRequest);

}
