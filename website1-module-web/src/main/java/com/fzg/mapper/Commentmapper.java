package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Commentmapper extends BaseMapper<Comment> {


    List<Comment> selectChildCommentPage(
            @Param("rootId") Long rootId,
            @Param("lastId") Long lastId,
            @Param("size") Integer size
    );

    void incrementReplyCount(@Param("rootId") Long rootId);

    int logicDeleteById(@Param("id") Long id);

    List<Long> selectRootIdsForPage(@Param("articleId") Long articleId, @Param("lastId") Long lastId, @Param("size") int size);

    List<Comment> selectRootsByIds(@Param("list") List<Long> rootIds);

    Integer countChildByRootId(@Param("rootId") Long rootId);
}