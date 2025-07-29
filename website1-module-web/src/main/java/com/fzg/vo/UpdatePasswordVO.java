package com.fzg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdatePasswordVO {

    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 20, groups = {UpdateUsernameVO.UpdateUsernameVOValidated.class})
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, groups = {UpdateUsernameVO.UpdateUsernameVOValidated.class})
    private String newPassword;
}
