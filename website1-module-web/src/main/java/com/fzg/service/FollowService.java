package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Follow;
import com.fzg.model.User;
import com.fzg.vo.FollowVO;
import com.fzg.vo.UserVO;

import java.util.List;

public interface FollowService extends IService<Follow> {
    Boolean addFollow(FollowVO follow);

    List<UserVO> queryFolList(FollowVO foVO);
}
