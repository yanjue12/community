package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@TableName(value ="operation_log")
@Data
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "操作管理员id")
    private Integer adminId;

    @Schema(description = "操作类型")
    private String operationType;


    @Schema(description = "操作目标表")
    private String targetTable;

    @Schema(description = "操作目标id")
    private Long targetId;

    @Schema(description = "操作描述")
    private String operationDetail;

    @Schema(description = "操作IP地址")
    private String ipAddress;

    @Schema(description = "操作时间")
    private Date createAt;

}
