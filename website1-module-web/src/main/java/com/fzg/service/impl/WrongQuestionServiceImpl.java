package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.WrongQuestionMapper;
import com.fzg.model.WrongQuestion;
import com.fzg.service.WrongQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class WrongQuestionServiceImpl
        extends ServiceImpl<WrongQuestionMapper, WrongQuestion>
        implements WrongQuestionService {

    @Override
    public void handleAnswer(Long userId,
                             Long paperId,
                             Long attemptId,
                             Long questionId,
                             String userAnswerJson,
                             boolean isCorrect) {
        if (userId == null || userId <= 0 || questionId == null) {
            return;
        }

        WrongQuestion existing = getOne(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getQuestionId, questionId),
                false
        );

        Date now = new Date();

        if (!isCorrect) {
            // 错题：新增或累计
            if (existing == null) {
                WrongQuestion wq = new WrongQuestion();
                wq.setUserId(userId);
                wq.setQuestionId(questionId);
                wq.setPaperId(paperId);
                wq.setAttemptId(attemptId);
                wq.setLastUserAnswer(userAnswerJson);
                wq.setWrongCount(1);
                wq.setMastered(0);
                wq.setMasteredAt(null);
                wq.setLastWrongAt(now);
                wq.setCreatedAt(now);
                wq.setUpdatedAt(now);
                save(wq);
            } else {
                existing.setPaperId(paperId);
                existing.setAttemptId(attemptId);
                existing.setLastUserAnswer(userAnswerJson);
                existing.setWrongCount(
                        (existing.getWrongCount() == null ? 0 : existing.getWrongCount()) + 1
                );
                // 已掌握后又错，回退为未掌握
                existing.setMastered(0);
                existing.setMasteredAt(null);
                existing.setLastWrongAt(now);
                existing.setUpdatedAt(now);
                updateById(existing);
            }
            return;
        }

        // 答对：若曾错过且未掌握，则自动标记为已掌握
        if (existing != null && (existing.getMastered() == null || existing.getMastered() == 0)) {
            existing.setMastered(1);
            existing.setMasteredAt(now);
            existing.setUpdatedAt(now);
            updateById(existing);
        }
    }

    @Override
    public boolean markMastered(Long userId, Long id) {
        WrongQuestion db = getById(id);
        if (db == null || !db.getUserId().equals(userId)) {
            return false;
        }
        db.setMastered(1);
        db.setMasteredAt(new Date());
        db.setUpdatedAt(new Date());
        return updateById(db);
    }

    @Override
    public boolean removeFromBook(Long userId, Long id) {
        WrongQuestion db = getById(id);
        if (db == null || !db.getUserId().equals(userId)) {
            return false;
        }
        return removeById(id);
    }
}
