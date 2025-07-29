package com.fzg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateUsernameVO {
    public interface UpdateUsernameVOValidated {
    }


    @NotBlank(groups = {UpdateUsernameVOValidated.class},message = "用户名不能为空")
    private String username;


}
