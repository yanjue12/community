package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SubtitleVO {

    @Schema(description = "子标题")
    private String subtitle;

    @Schema(description = "描述")
    private String description;


}
