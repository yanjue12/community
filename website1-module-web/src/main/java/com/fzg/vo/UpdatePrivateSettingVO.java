package com.fzg.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fzg.model.UserPrivacy;
import lombok.Data;

@Data
public class UpdatePrivateSettingVO extends UserPrivacy {

    @TableField(exist = false)
    private String flag;

    @TableField(exist = false)
    private Long articleId;
}
