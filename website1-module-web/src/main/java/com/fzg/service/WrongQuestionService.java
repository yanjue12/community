package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.WrongQuestion;

public interface WrongQuestionService extends IService<WrongQuestion> {

    /**
     * 答题判分钩子：交卷时为每一题调用。
     * - isCorrect=false：upsert 错题记录，wrong_count+1，刷新 last_user_answer/last_wrong_at；
     *   若已存在并 mastered=1，则视为重新做错，回退为未掌握。
     * - isCorrect=true：若用户此题之前是未掌握的错题，自动置 mastered=1（已掌握）。
     */
    void handleAnswer(Long userId,
                      Long paperId,
                      Long attemptId,
                      Long questionId,
                      String userAnswerJson,
                      boolean isCorrect);

    /** 用户主动标记某条错题为已掌握 */
    boolean markMastered(Long userId, Long id);

    /** 用户从错题本移除（物理删除） */
    boolean removeFromBook(Long userId, Long id);
}
