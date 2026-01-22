package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.model.User;
import com.fzg.model.Result;
import com.fzg.vo.*;

import java.util.List;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-09 17:09:29
*/
public interface UserService extends IService<User> {

    Result register(RegisterVO registerVO);

    Result sendVerificationCode(RegisterVO registerVO);

    Result updateUsername(Integer userId, UpdateUsernameVO updateUsernameVO);

    Result updatePassword(Long userId, UpdatePasswordVO updatePasswordVO);

    Result forgetPassword(ForgetPasswordVO forgetPasswordVO);

    Result login(UserLoginVO userLoginVO);

    Result checkUsername(RegisterVO request);

    Result sendCode(EmailRequest emailRequest);

    List<UserVO> queryActiveUser(Integer days, Integer size);

    Boolean publishArticle(Article articleVO);

    Boolean updateArticle(Article articleVO);
}
