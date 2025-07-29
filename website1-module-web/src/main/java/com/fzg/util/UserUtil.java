package com.fzg.util;

/**
 * 用户工具类
 */
public class UserUtil {
    /**
     * 获取用户盐值，用于加密用户密码
     * @param account
     * @return
     */
    public static String getUserSalt(String account){
        // 盐值
        String[] salts = {"sun","moon","star","sky","cloud","fog","rain","wind","rainbow"};
        int hashCode = account.hashCode() + 159;
        int mod = Math.abs( hashCode % 9 );
        return salts[mod];
    }

    /**
     * 获取用户加密密码
     * @param account
     * @param password
     * @return
     */
    public static String getUserEncryptPassword(String account, String password){
        String pwdAndSalt = password + getUserSalt(account);
        return MD5Util.MD5Encode(pwdAndSalt,"utf8");
    }
}