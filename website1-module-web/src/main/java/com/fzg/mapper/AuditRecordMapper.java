package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
    @Select("select * from audit_record where article_id = #{articleId} limit 1")
    AuditRecord selectByArticleId(@Param("articleId") Long articleId);

}
