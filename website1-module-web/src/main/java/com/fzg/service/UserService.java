package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.User;
import com.fzg.model.Result;
import com.fzg.vo.RegisterVO;
import com.fzg.vo.UserLoginVO;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-09 17:09:29
*/
public interface UserService extends IService<User> {
    Result accountLogin(UserLoginVO userLoginVO);

    Result register(RegisterVO registerVO);

    Result sendVerificationCode(RegisterVO registerVO);
}
