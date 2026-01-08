package com.fzg.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.SubtitlesMapper;
import com.fzg.model.Subtitles;
import com.fzg.service.SubtitlesService;
import org.springframework.stereotype.Service;

@Service
public class SubtitlesServiceImpl extends ServiceImpl<SubtitlesMapper, Subtitles> implements SubtitlesService {
}
