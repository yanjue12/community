package com.fzg.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NewsDetailsVO {

    @Schema(description = "新闻内容")
    @NotBlank(message = "新闻内容不能为空")
    private String content;




}
