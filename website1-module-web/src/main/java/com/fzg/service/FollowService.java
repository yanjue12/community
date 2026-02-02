package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Follow;
import com.fzg.vo.FollowVO;

public interface FollowService extends IService<Follow> {
    Boolean addFollow(FollowVO follow);
}
