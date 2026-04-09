package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.PaperAnswerMapper;
import com.fzg.model.PaperAnswer;
import com.fzg.service.PaperAnswerService;
import org.springframework.stereotype.Service;

@Service
public class PaperAnswerServiceImpl extends ServiceImpl<PaperAnswerMapper, PaperAnswer> implements PaperAnswerService {
}
