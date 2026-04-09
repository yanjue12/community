package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.PaperMapper;
import com.fzg.model.Paper;
import com.fzg.service.PaperService;
import org.springframework.stereotype.Service;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
}
