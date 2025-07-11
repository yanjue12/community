package com.fzg.constant;

/**
 * 邮箱验证码缓存key
 */
public class RedisVerificationKey {

    public static final String getVerificationCodeKey(String email){
         return String.format("vCode-%s",email);
    }

}
