package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@TableName("user")
@Schema(name = "用户表", description = "用户表")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;                // 用户ID
    private String username;        // 用户名
    private String password;        // 密码
    private String email;           // 邮箱
    private String phone;           // 手机号
    private String nickname;        // 昵称
    private String avatar;          // 头像URL
    private String gender;          // 性别 0:未知 1:男 2:女
    private Date birthday;          // 生日
    private String introduction;    // 个人简介
    private String website;         // 个人网站
    private String location;        //所在地
    private String company;         // 公司
    private String position;        // 职位
    private String signature;       // 个性签名
    private Integer score;          // 积分
    private Integer level;          // 等级
    private Integer experience;     // 经验值
    private Integer gold;           // 银币
    private Integer topicCount;     // 发帖数
    private Integer commentCount;   // 评论数
    private Integer followerCount;  // 粉丝数
    private Integer followingCount; // 关注数
    private Integer collectionCount; // 收藏数
    private String status;          // 状态 0:正常 1:禁用 2:未激活
    private String emailVerified;   // 邮箱验证
    private String phoneVerified;    // 手机验证
    private Date lastLoginTime;     // 最后登录时间
    private String lastLoginIp;     // 最后登录IP
    private Date createdAt;         // 创建时间
    private Date updatedAt;         // 更新时间
    private String coverImages;


}
