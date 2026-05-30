package com.fzg.constant;

/**
 * 邮箱验证码缓存key
 */
public class RedisVerificationKey {

    public static String getVerificationCodeKey(String email){
         return String.format("vCode-%s",email);
    }

    public static String getSmsCodeKey(String phoneNumber){
        return String.format("smsCode-%s", phoneNumber);
    }

    public static String getRegisterCaptchaKey(String captchaId){
        return String.format("register-captcha-%s", captchaId);
    }

}
