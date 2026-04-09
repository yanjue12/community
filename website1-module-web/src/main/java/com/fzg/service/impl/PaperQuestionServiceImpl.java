package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.PaperQuestionMapper;
import com.fzg.model.PaperQuestion;
import com.fzg.service.PaperQuestionService;
import org.springframework.stereotype.Service;

@Service
public class PaperQuestionServiceImpl extends ServiceImpl<PaperQuestionMapper, PaperQuestion> implements PaperQuestionService {
}
