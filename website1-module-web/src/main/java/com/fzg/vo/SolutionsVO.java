package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SolutionsVO {

    @Schema(description = "ID")
    private Integer id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "图片不能为空")
    private String url;


//    @Schema(description = "简介")
//    private String introduction;

    @Schema(description = "子标题列表")
    private List<SubtitleVO> subtitlesVOList;


}
