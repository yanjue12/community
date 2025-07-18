package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Subtitles;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubtitlesMapper extends BaseMapper<Subtitles> {
    //boolean updateBatch(List<Subtitles> subtitlesList);

    boolean insertBatch(List<Subtitles> subtitlesList);
}
