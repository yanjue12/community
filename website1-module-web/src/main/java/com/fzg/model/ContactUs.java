package com.fzg.model; // 修改包名为正确路径

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;

/**
 * 联系记录表
 * @TableName contact_us
 */
@TableName(value ="contact_us")
@Data
@Schema(name = "ContactUs", description = "联系记录表")
public class ContactUs {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 
     */
    private String company;

    /**
     * 
     */
    private String phone;

    /**
     * 
     */
    private String message;

    /**
     * 
     */
    private Date createdAt;


}