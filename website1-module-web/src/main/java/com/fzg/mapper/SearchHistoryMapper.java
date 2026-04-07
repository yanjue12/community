package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.SearchHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    /**
     * 查询用户某个关键词的历史记录
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} AND search_term = #{searchTerm} LIMIT 1")
    SearchHistory selectByUserAndTerm(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * 更新已有记录的最后搜索时间
     */
    @Update("UPDATE search_history SET last_searched_at = NOW() WHERE user_id = #{userId} AND search_term = #{searchTerm}")
    int updateLastSearchedAt(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * 查询用户搜索历史，按最后搜索时间倒序，只取前15条的ID
     */
    @Select("SELECT id FROM search_history WHERE user_id = #{userId} ORDER BY last_searched_at DESC LIMIT 15")
    List<Long> selectTop15IdsByUser(@Param("userId") Long userId);

    /**
     * 物理删除用户不在保留列表中的历史记录
     */
    @Delete("<script>" +
            "DELETE FROM search_history WHERE user_id = #{userId}" +
            "<if test='keepIds != null and keepIds.size() > 0'>" +
            " AND id NOT IN " +
            "<foreach collection='keepIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    int deleteExceedingHistory(@Param("userId") Long userId, @Param("keepIds") List<Long> keepIds);

    /**
     * 查询所有有搜索历史的用户ID（去重）
     */
    @Select("SELECT DISTINCT user_id FROM search_history")
    List<Long> selectDistinctUserIds();

    /** 查询用户最近15条搜索历史，按最后搜索时间倒序 */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY last_searched_at DESC LIMIT 15")
    List<SearchHistory> selectTop15ByUser(@Param("userId") Long userId);

    /** 删除用户指定的一条搜索历史（校验归属） */
    @Delete("DELETE FROM search_history WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 删除用户所有搜索历史 */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    int deleteAllByUser(@Param("userId") Long userId);
}
