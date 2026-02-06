package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 审核表
 * 对应数据库表：audit_record
 *
 * @author fzg
 * @date 2026-02-06
 */
@Data
@TableName("audit_record") // MyBatis-Plus表名映射（非MP可删除）
public class AuditRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO) // MyBatis-Plus自增主键（非MP可删除）
    private Long id;

    /**
     * 业务类型（ARTICLE）
     */
    private String bizType;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 审核状态：0待审核 1通过 2拒绝
     */
    private Byte auditStatus;

    /**
     * 审核类型：1自动 2人工
     */
    private Byte auditType;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核拒绝原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    // 可选：添加状态/类型的常量枚举（推荐，避免硬编码数字）
    public static class AuditStatus {
        public static final Byte PENDING = 0; // 待审核
        public static final Byte PASS = 1;   // 通过
        public static final Byte REJECT = 2; // 拒绝
    }

    public static class AuditType {
        public static final Byte AUTO = 1;  // 自动审核
        public static final Byte MANUAL = 2;// 人工审核
    }
}