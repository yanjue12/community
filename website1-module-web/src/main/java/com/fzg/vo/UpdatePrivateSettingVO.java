package com.fzg.vo;


import com.fzg.model.UserPrivacy;
import lombok.Data;

@Data
public class UpdatePrivateSettingVO extends UserPrivacy {

    private String flag;//全局 还是 单篇文章
    private Long articleId;
}
