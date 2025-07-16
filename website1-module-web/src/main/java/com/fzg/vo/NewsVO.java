package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NewsVO {

    public interface NewsVOValidated{

    }

    @Schema(description = "标题")
    @NotBlank(groups = {NewsVO.NewsVOValidated.class},message = "新闻标题不能为空")
    private String title;

    @Schema(description = "摘要")
    @NotBlank(groups = {NewsVO.NewsVOValidated.class},message = "新闻摘要不能为空")
    private String summary;

    @Schema(description = "新闻标签")
    @NotBlank(groups = {NewsVO.NewsVOValidated.class},message = "新闻标签不能为空")
    private Short label;

    private Stirng
}
