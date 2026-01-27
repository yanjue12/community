package com.fzg.vo;


import lombok.Data;

@Data
public class UpdatePrivateSettingVO {
    private Long userId;
    private Long articleId;
    private String allArticle;//0 所有文章 1 单独文章
    private String visibility;//可见性，0公开 1私密 2仅粉丝 3仅互相关注
    private String isCommentable;//0 可以评论，1不可评论，2粉丝可评论，3互相关注可评论

}
