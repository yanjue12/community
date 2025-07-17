package com.fzg.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class NewsCreateBO {

    @Schema(description = "新闻ID")
    private Integer id;

    @Schema(description = "标题")
    @NotBlank(message = "新闻标题不能为空")
    private String title;

    @Schema(description = "摘要")
    @NotBlank(message = "新闻摘要不能为空")
    private String summary;

    @Schema(description = "新闻标签")
    @NotBlank(message = "新闻标签不能为空")
    private String label;

    @Schema(description = "新闻发布时间")
    private LocalDateTime publishDate;


    @Schema(description = "新闻内容")
    @NotBlank(message = "新闻内容不能为空")
    private String content;
}
