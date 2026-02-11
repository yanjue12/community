package com.fzg.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagWeightDTO {
    private Long tagId;
    private Double weight;
}
