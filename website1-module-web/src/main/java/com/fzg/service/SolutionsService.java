package com.fzg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Result;
import com.fzg.model.Solutions;
import com.fzg.vo.SolutionsVO;

import java.util.List;

/**
* @author yanju
* @description 针对表【solutions(solution表)】的数据库操作Service
* @createDate 2025-07-09 17:09:29
*/
public interface SolutionsService extends IService<Solutions> {

    Page<SolutionsVO> solutionsList(Integer pageNumber, Integer pageSize);

    Result addSolutions(SolutionsVO solutionsVO);

    Result updateSolutions(Integer id, SolutionsVO solutionsVO);

    Result deleteSolutions(Integer id);

    Result changeSolutionsState(Integer id, Short state);


    Result batchDeleteSolutions(List<Integer> ids);

    Result<List<SolutionsVO>> AdminSolutionsList();
}
