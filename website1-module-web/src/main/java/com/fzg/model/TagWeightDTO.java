package com.fzg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class TagWeightDTO {

    private Long tagId;

    private Double weight;

    private Long lastUpdateTime; // 新增（时间衰减用）

    // 构造方法
    public TagWeightDTO(Long tagId, Double weight) {
        this.tagId = tagId;
        this.weight = weight;
    }

    public TagWeightDTO(Long tagId, Double weight, Long lastUpdateTime) {
        this.tagId = tagId;
        this.weight = weight;
        this.lastUpdateTime = lastUpdateTime;
    }
}
