package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Question;
import com.fzg.vo.QuestionQueryRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    List<Question> queryQuestionList(@Param("request") QuestionQueryRequest request,
                                     @Param("offset") int offset);

    Long countQuestionList(@Param("request")QuestionQueryRequest request);

    List<String> selectTagPayloads(@Param("limit") Integer limit);

}
