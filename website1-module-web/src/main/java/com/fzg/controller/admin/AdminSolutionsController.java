package com.fzg.controller.admin;


import com.fzg.model.Result;
import com.fzg.service.SolutionsService;
import com.fzg.vo.SolutionsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员解决方案管理接口
 */
@RestController
@RequestMapping("/admin/solutions")
@Schema(description = "管理员解决方案管理接口")
@RequiredArgsConstructor
public class AdminSolutionsController {

    private final SolutionsService solutionsService;

    //获取解决方案列表
    @GetMapping("/list")
    public Result listSolutions() {
        return solutionsService.AdminSolutionsList();
    }

    @PostMapping("/add")
    public Result addSolutions(@RequestBody SolutionsVO solutionsVO) {

        return solutionsService.addSolutions(solutionsVO);
    }


    //修改解决方案
    @PutMapping("/update/{id}")
    public Result updateSolutions(@PathVariable Integer id, @RequestBody SolutionsVO solutionsVO) {
        return solutionsService.updateSolutions(id, solutionsVO);
    }

    /**
     *删除解决方案
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteSolutions(@PathVariable Integer id) {
        return solutionsService.deleteSolutions(id);
    }


    //批量删除解决方案
    @DeleteMapping("/batchDelete")
    public Result batchDeleteSolutions(@RequestBody List<Integer> ids) {
        return solutionsService.batchDeleteSolutions(ids);
    }


    // 修改解决方案状态
    @PutMapping("/changeState/{id}")
    public Result changeSolutionsState(@PathVariable Integer id, @RequestParam Short state) {
        return solutionsService.changeSolutionsState(id, state);
    }



}
