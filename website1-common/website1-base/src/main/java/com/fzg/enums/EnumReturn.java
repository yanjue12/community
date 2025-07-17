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

        PASSWORD_ERROR(2006, "密码错误"),

        EMAIL_ALREADY_REGISTERED(2007,"邮箱已被注册"),
        REGISTER_FAIL(2008, "注册失败"),

        USER_DISABLED(2009, "账号已被禁用"),


        VERIFICATION_CODE_FREQUENT(2011, "验证码发送频繁，请稍后再试"),
        VERIFICATION_CODE_ERROR(2012, "验证码错误"),

        NEWS_NOT_EXIST(2021, "新闻不存在"),
        NEWS_DETAIL_NOT_FOUND(2022, "新闻详情不存在"),
        NEWS_CREATE_ERROR(2023, "创建新闻失败"),


        CONTENT_NOT_EXISTS(2031, "内容不能为空"),
        NAME_NOT_EXISTS(2032, "姓名不能为空"),


        PARAMS_EMPTY(2041, "参数不能为空"),
        SOLUTIONS_SAVE_ERROR(2042, "解决方案保存失败"),
        SOLUTIONS_NOT_FOUND(2043, "解决方案不存在"),
        SOLUTIONS_UPDATE_ERROR(2044, "解决方案更新失败"),
        SOLUTIONS_DELETE_ERROR(2045, "解决方案删除失败"),
        SOLUTIONS_LIST_ERROR(2046, "解决方案列表获取失败"),

        OPERATION_SUCCESS(2999,"操作成功"),

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