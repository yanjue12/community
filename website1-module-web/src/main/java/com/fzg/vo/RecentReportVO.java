package com.fzg.vo;

import lombok.Data;
import java.util.Date;

/**
 * 最近处理记录 VO
 */
@Data
public class RecentReportVO {

    private Long id;
    private String status;
    private String statusText;
    private String targetType;
    private String targetTypeText;
    private String reasonType;
    private String reasonName;
    private String targetTitle;
    private String adminRemark;
    private String reporterName;
    private String adminName;
    private Date createdAt;
    private Date processedAt;
}
