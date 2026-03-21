package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.PrivateConversation;
import com.fzg.vo.ConversationVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateConversationMapper extends BaseMapper<PrivateConversation> {
    PrivateConversation selectByUsers(Long min, Long max);

    int insert(PrivateConversation conv);

    int updateLastMessage(Long id, Long msgId, String content);

    List<PrivateConversation> selectByUser(Long userId, int offset, int limit);

    List<ConversationVO> selectConversationList(@Param("userId") Long userId);
}
