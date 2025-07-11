package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * solution表
 * @TableName solutions
 */
@TableName(value ="solutions")
@Data
public class Solutions {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String content;

    /**
     * 图片url
     */
    private String imageUrl;

    /**
     * 挑战列表
     */
    private Object challenges;

    /**
     * 服务列表
     */
    private Object offerings;

    /**
     * 成功案例列表
     */
    private Object stories;

    /**
     * 0-下架 1-上架
     */
    private Integer states;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Solutions other = (Solutions) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getImageUrl() == null ? other.getImageUrl() == null : this.getImageUrl().equals(other.getImageUrl()))
            && (this.getChallenges() == null ? other.getChallenges() == null : this.getChallenges().equals(other.getChallenges()))
            && (this.getOfferings() == null ? other.getOfferings() == null : this.getOfferings().equals(other.getOfferings()))
            && (this.getStories() == null ? other.getStories() == null : this.getStories().equals(other.getStories()))
            && (this.getStates() == null ? other.getStates() == null : this.getStates().equals(other.getStates()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getImageUrl() == null) ? 0 : getImageUrl().hashCode());
        result = prime * result + ((getChallenges() == null) ? 0 : getChallenges().hashCode());
        result = prime * result + ((getOfferings() == null) ? 0 : getOfferings().hashCode());
        result = prime * result + ((getStories() == null) ? 0 : getStories().hashCode());
        result = prime * result + ((getStates() == null) ? 0 : getStates().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", imageUrl=").append(imageUrl);
        sb.append(", challenges=").append(challenges);
        sb.append(", offerings=").append(offerings);
        sb.append(", stories=").append(stories);
        sb.append(", states=").append(states);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}