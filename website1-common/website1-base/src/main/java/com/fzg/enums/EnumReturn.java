package com.fzg.enums;

import lombok.Getter;

/**
 * 返回枚举
 */
@Getter
public enum EnumReturn {

        // 负数 异常
        STATUS_SYSTEM_LOG_ERROR(-3, "后台日志错误"),

        OPERATION_FAIL(-1,"操作失败"),
        SERVER_INNER_ERROR(-222,"服务内部异常"),

        // 1000 系统级别
        STATUS_NAME_REPETITION(1001, "数据重复"),

        // 2000 用户操作
        USERNAME_NOT_EXISTS(2001, "用户名不存在或错误，需要注册"),
        ACCOUNT_NOT_EXISTS(2002, "账号不存在或错误，需要注册"),
        EMAIL_NOT_EXISTS(2003, "邮箱不存在或错误，需要注册"),

        USERNAME_PASSWORD_ERROR(2004, "账号或密码错误"),
        USERNAME_PASSWORD_EMPTY(2005, "账号或密码不能为空"),

        EMAIL_ALREADY_REGISTERED(2010,"邮箱已被注册"),

        USER_DISABLED(2033, "账号已被禁用"),

        VERIFICATION_CODE_FREQUENT(2041, "验证码发送频繁，请稍后再试"),

        // 4000 权限相关
        UN_AUTHORIZATION(401,"未授权"),
        ACCESS_DENY_FAIL(403, "权限不足"),


        //4000 项目相关
        //NOT_APPLY_TIMESORT(4001, "签到成功，但是没有申请入场时间，无法排队"),

        ;


    EnumReturn(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;

    private String desc;

       /* private String value;
        private int key;

        EnumReturn(int key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getKey() {
            return key;
        }


        public static String getValue(Integer key) {
            for(EnumReturn item : EnumReturn.values()){
                if(key.equals(item.getKey())){
                    return item.value;
                }
            }
            return null;}*/
        }