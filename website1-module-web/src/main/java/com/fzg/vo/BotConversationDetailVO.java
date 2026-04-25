package com.fzg.vo;

import lombok.Data;

import java.util.List;

@Data
public class BotConversationDetailVO {
    private BotConversationSessionVO session;
    private List<BotHistoryItemVO> messages;
}
