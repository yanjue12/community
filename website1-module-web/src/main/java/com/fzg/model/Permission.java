package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限表
 */
@Data
@TableName("permission")
public class Permission implements Serializable {
    
    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限类型 MENU:菜单 BUTTON:按钮 API:接口
     */
    private String type;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 请求方法 GET POST PUT DELETE
     */
    private String method;

    /**
     * 接口路径
     */
    private String apiPath;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0:禁用 1:启用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}
