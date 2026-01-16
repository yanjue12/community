package com.fzg.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("like_record")
public class LikeRecord {
    private Long id;
    private Long userId;
    private Long articleId;
    private String articleType;
    private String status;
    private Date createAt;
    private Date updateAt;
}
