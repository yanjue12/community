package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.PrivateMessage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
    int insert(PrivateMessage msg);

    List<PrivateMessage> selectByConversation(Long conversationId, int offset, int limit);

    int countByConversation(Long conversationId);

    int markRead(Long conversationId, Long userId);

    int countUnread(Long userId);
}
