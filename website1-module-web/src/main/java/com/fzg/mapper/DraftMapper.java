package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Draft;
import com.fzg.vo.DraftVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DraftMapper extends BaseMapper<Draft> {

    List<DraftVO> queryArticleDraft(@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);
}