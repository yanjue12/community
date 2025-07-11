package com.fzg.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisVerificationKey;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.service.UserService;
import com.fzg.mapper.UserMapper;
import com.fzg.vo.RegisterVO;
import com.fzg.vo.UserLoginVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Result accountLogin(UserLoginVO userLoginVO) {
        if (null == userLoginVO) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }



        String username = userLoginVO.getUsername();
        String password = userLoginVO.getPassword();
        //判断用户名是否正确 || 存在
        if(StrUtil.isNotEmpty(username)){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username",username);
            User one = this.getOne(queryWrapper);
            if(null == one){
                return Result.fail(EnumReturn.USERNAME_NOT_EXISTS);
            }
        }
        //判断账号是否存在 || 正确
        if(StrUtil.isNotEmpty(userLoginVO.getAccount())){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("account",userLoginVO.getAccount());
            if(null == this.getOne(queryWrapper)){
                return Result.fail(EnumReturn.ACCOUNT_NOT_EXISTS);
            }
        }

        //判断邮箱是否存在 || 正确
        if(StrUtil.isNotEmpty(userLoginVO.getEmail())){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email",userLoginVO.getEmail());
            if(null == this.getOne(queryWrapper)){
                return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);}
        }

        //判断用户状态
        if(userLoginVO.getStatus() == 1){
            return Result.fail(EnumReturn.USER_DISABLED);
        }


        //TODO 判断密码

        return Result.success(400);

    }


    /**
     * 注册 校验验证码 密码 邮箱
     * @param registerVO
     * @return
     */
    @Override
    public Result register(RegisterVO registerVO) {
        if (null == registerVO) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }




        return null;
    }


    @Override
    public Result sendVerificationCode(RegisterVO registerVO) {
        String email = registerVO.getEmail();
        //邮箱已被注册过
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail,email);
        if(null != this.getOne(queryWrapper)){
            return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
        }

        String verificationCode = RandomUtil.randomNumbers(6);
        String vCodeKey = RedisVerificationKey.getVerificationCodeKey(email);


        //检查是否频繁
        Long expire = redisTemplate.getExpire(vCodeKey);
        if(expire != null && expire >= 240){
            return Result.fail(EnumReturn.VERIFICATION_CODE_FREQUENT);
        }

        //保存到redis
        redisTemplate.opsForValue().set(vCodeKey ,verificationCode,5, TimeUnit.MINUTES);


        //发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setSubject(verificationCode);
        message.setTo("您的验证码是："+ verificationCode + "，有效期为5分钟");


        return Result.success("邮件成功发送");

    }

}




