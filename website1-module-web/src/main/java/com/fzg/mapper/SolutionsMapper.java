package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Solutions;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yanju
* @description 针对表【solutions(solution表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:29
* @Entity model.Solutions
*/
@Mapper
public interface SolutionsMapper extends BaseMapper<Solutions> {

}




