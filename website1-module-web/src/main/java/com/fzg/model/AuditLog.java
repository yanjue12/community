package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核历史表
 * 对应数据库表：audit_log
 *
 * @author fzg
 * @date 2026-02-06
 */
@Data
@TableName("audit_log") // MyBatis-Plus表名映射，非MP可删除
public class AuditLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO) // MyBatis-Plus自增主键，非MP可删除
    private Long id;

    /**
     * 关联审核表ID（audit_record.id）
     */
    private Long auditId;

    /**
     * 审核操作：1自动通过 2自动拒绝 3人工通过 4人工拒绝
     */
    private Byte action;

    /**
     * 审核人ID（人工审核时非空，自动审核可为空）
     */
    private Long auditorId;

    /**
     * 审核原因（拒绝时必填，通过可空）
     */
    private String reason;

    /**
     * 创建时间（操作执行时间）
     */
    private LocalDateTime createdAt;

    // 推荐：内置操作类型常量，避免代码硬编码魔法数字
    public static class ActionType {
        public static final Byte AUTO_PASS = 1;    // 自动通过
        public static final Byte AUTO_REJECT = 2;  // 自动拒绝
        public static final Byte MANUAL_PASS = 3;  // 人工通过
        public static final Byte MANUAL_REJECT = 4;// 人工拒绝
    }
}