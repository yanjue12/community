package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.QuestionMapper;
import com.fzg.model.Question;
import com.fzg.service.QuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}
