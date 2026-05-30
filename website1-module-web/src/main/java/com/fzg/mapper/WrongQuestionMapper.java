package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.WrongQuestion;
import com.fzg.vo.WrongQuestionQueryRequest;
import com.fzg.vo.WrongQuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    /** 错题本分页列表（带题干、解析、试卷标题等） */
    List<WrongQuestionVO> queryWrongList(@Param("request") WrongQuestionQueryRequest request,
                                         @Param("offset") int offset);

    /** 错题本总数 */
    Long countWrongList(@Param("request") WrongQuestionQueryRequest request);

    /** 错题详情 */
    WrongQuestionVO queryWrongDetail(@Param("userId") Long userId,
                                     @Param("id") Long id);

    /** 按知识点 tag 聚合统计错题数（用于"薄弱知识点"图表） */
    List<Map<String, Object>> countByTag(@Param("userId") Long userId);
}
