package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.ArticleViewHistory;
import com.fzg.vo.ArticleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleViewHistoryMapper extends BaseMapper<ArticleViewHistory> {

    @Delete("DELETE FROM article_view_history WHERE created_at < #{cutoffTime}")
    int deleteBeforeDate(@Param("cutoffTime") java.util.Date cutoffTime);

    /** 查询用户最近15条浏览历史，按时间倒序 */
    @Select("SELECT * FROM article_view_history WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 15")
    List<ArticleViewHistory> selectTop15ByUser(@Param("userId") Long userId);

    /** 查询用户对某篇文章的浏览记录（用于判断是否已存在） */
    @Select("SELECT * FROM article_view_history WHERE user_id = #{userId} AND article_id = #{articleId} LIMIT 1")
    ArticleViewHistory selectByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 更新已有浏览记录的时间 */
    @Update("UPDATE article_view_history SET created_at = NOW() WHERE user_id = #{userId} AND article_id = #{articleId}")
    int updateViewTime(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 查询用户最近15条浏览历史，关联文章数据返回（含作者信息） */
    @Select("SELECT a.id, a.title, a.summary, a.cover_image AS coverImage, a.type, " +
            "a.updated_at AS updatedAt, a.published_at AS publishedAt, " +
            "a.status, a.is_top AS isTop, a.is_recommend AS isRecommend, " +
            "a.view_count AS viewCount, a.like_count AS likeCount, a.comment_count AS commentCount, " +
            "a.collect_count AS collectCount, a.share_count AS shareCount, " +
            "a.user_id AS userId, u.avatar, u.nickname AS nickName, " +
            "c.name AS categoryName " +
            "FROM article_view_history h " +
            "INNER JOIN article a ON h.article_id = a.id " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE h.user_id = #{userId} " +
            "ORDER BY h.created_at DESC LIMIT 15")
    List<ArticleVO> selectTop15ArticlesByUser(@Param("userId") Long userId);

    /** 删除用户指定的一条浏览记录（校验归属） */
    @Delete("DELETE FROM article_view_history WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 删除用户所有浏览记录 */
    @Delete("DELETE FROM article_view_history WHERE user_id = #{userId}")
    int deleteAllByUser(@Param("userId") Long userId);
}