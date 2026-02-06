package com.fzg.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Document(indexName = "article")
public class ArticleEs {

    @Id
    private Long id;

    @Field(type = FieldType.Text,
           analyzer = "ik_max_analyzer",
           searchAnalyzer = "ik_smart_analyzer")
    private String title;

    @Field(type = FieldType.Text,
           analyzer = "ik_max_analyzer",
           searchAnalyzer = "ik_smart_analyzer")
    private String content;

    @Field(type = FieldType.Keyword)
    private String[] tags;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Date,
           format = {},
           pattern = "yyyy-MM-dd HH:mm:ss||epoch_millis")
    private Date createTime;
}
