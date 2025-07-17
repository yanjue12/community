package com.fzg.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
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
import com.fzg.util.UserUtil;
import com.fzg.vo.RegisterVO;
import com.fzg.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JavaMailSender javaMailSender;

    @Override
    public Result accountLogin(UserLoginVO userLoginVO) {
        if (null == userLoginVO) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }

        User user = null;

        String username = userLoginVO.getUsername();
        String password = userLoginVO.getPassword();
        //判断用户名是否正确 || 存在
        if(StrUtil.isNotEmpty(username)){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username",username);
            user = this.getOne(queryWrapper);
            if(null == user){
                return Result.fail(EnumReturn.USERNAME_NOT_EXISTS);
            }
        }
        //判断账号是否存在 || 正确
        if(StrUtil.isNotEmpty(userLoginVO.getAccount())){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("account",userLoginVO.getAccount());
            user = this.getOne(queryWrapper);
            if(null == user){
                return Result.fail(EnumReturn.ACCOUNT_NOT_EXISTS);
            }
        }

        //判断邮箱是否存在 || 正确
        if(StrUtil.isNotEmpty(userLoginVO.getEmail())){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email",userLoginVO.getEmail());
            user = this.getOne(queryWrapper);
            if(null == user){
                return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);}
        }

        //判断用户状态
        if(user.getStates() == 2){
            return Result.fail(EnumReturn.USER_DISABLED);
        }
        log.info("################## user:{}",user);



        log.info("################## password:{}",password);
        String encryptPwd = UserUtil.getUserEncryptPassword(user.getAccount(), userLoginVO.getPassword());
        if(!user.getPassword().equals(encryptPwd)){
            return Result.fail(EnumReturn.PASSWORD_ERROR);
        }




        log.info("################## user:{}",user);

        //登录成功，记录token
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        tokenInfo.setTokenTimeout(3600);
        SaSession session = StpUtil.getSession();
        session.set("USER_ID",user.getId());
        session.set("role",user.getRole());


        log.info("################## tokenInfo:{}",tokenInfo);

        Map<String,Object> response = new HashMap<>();
        response.put("role",user.getRole());
        response.put("tokenInfo",tokenInfo);

        return Result.success(response);

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

        String email = registerVO.getEmail();
        log.info("################## email:{}",email);
        if(StrUtil.isEmpty(email)){
            return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
        }

        //从redis中获取验证码
        String verificationCode = (String) redisTemplate.opsForValue()
                .get(RedisVerificationKey.getVerificationCodeKey(email));

        if(verificationCode == null || !verificationCode.equals(registerVO.getVerificationCode())){
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }

        //生成账号
        String r = RandomUtil.randomString(6);
        String timestampLastSix = String.valueOf(System.currentTimeMillis()).substring(6);
        String account = r+timestampLastSix;
        log.info("account:{}####################",account);


        //密码加密
        String encryptPwd = UserUtil.getUserEncryptPassword(account, registerVO.getPassword());
        log.info("encryptPwd:{}####################",encryptPwd);

        //TODO 设置默认头像


        //保存用户
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(encryptPwd);
        user.setAccount(account);
        user.setStates((short) 1);

        if(this.save(user)){
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

            tokenInfo.setTokenTimeout(3600);
            SaSession session = StpUtil.getSession();
            session.set("USER_NAME",user.getUsername());
            session.set("USER_ID",user.getId());
            return Result.success(tokenInfo);
        }else{
            return Result.fail(EnumReturn.REGISTER_FAIL);
        }

    }


    @Override
    public Result sendVerificationCode(RegisterVO registerVO) {
        String email = registerVO.getEmail();
        //邮箱已被注册过
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail,email);
        if(null != this.getOne(queryWrapper)){
            return Result.fail(EnumReturn.EMAIL_ALREADY_REGISTERED);
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
        message.setFrom("yanjue2024@163.com");
        message.setSubject("验证码");
        message.setTo(email);
        message.setText("验证码："+verificationCode+"有效期5分钟");

        log.info("################## 验证码：{}",verificationCode);

        try {
            javaMailSender.send(message);
            return Result.success("邮件成功发送");
        } catch (Exception e) {
            log.error("################## 邮件发送失败：{},邮箱：{}",e.getMessage(),email);
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }


    }

}




