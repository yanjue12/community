package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Solutions;
import com.fzg.vo.SolutionsVO;

import java.util.List;

/**
* @author yanju
* @description 针对表【solutions(solution表)】的数据库操作Service
* @createDate 2025-07-09 17:09:29
*/
public interface SolutionsService extends IService<Solutions> {

    List<SolutionsVO> solutionsList();
}
