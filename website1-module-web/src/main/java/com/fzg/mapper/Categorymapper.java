package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Category;
import com.fzg.vo.CategoryAdminVO;
import com.fzg.vo.CategoryQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Categorymapper extends BaseMapper<Category> {

    List<CategoryAdminVO> queryCategoryList(@Param("req") CategoryQueryRequest req,
                                            @Param("offset") Integer offset);

    Long countCategoryList(@Param("req") CategoryQueryRequest req);
}
