package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("dictionary")
public class Dictionary {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String dictType;
    private String dictValue;
    private String description;
    private Date createAt;
    private Date updateAt;
}
