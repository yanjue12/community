package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Favorite;
import com.fzg.model.Result;
import com.fzg.vo.LikeRequest;

public interface FavoriteService extends IService<Favorite> {
    Result articleCollect(LikeRequest likeRequest);
}
