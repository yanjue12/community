package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Paper;
import com.fzg.vo.PaperQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {
    List<Paper> selectPaperList(@Param("request") PaperQueryRequest request, @Param("offset") int offset);

    int selectPaperCount(@Param("request") PaperQueryRequest request);
}
