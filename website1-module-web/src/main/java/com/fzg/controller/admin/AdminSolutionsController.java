package com.fzg.controller.admin;


import com.fzg.model.Result;
import com.fzg.service.SolutionsService;
import com.fzg.vo.SolutionsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员解决方案管理接口
 */
@RestController
@RequestMapping("/admin/solutions")
@Schema(description = "管理员解决方案管理接口")
@RequiredArgsConstructor
public class AdminSolutionsController {

    private final SolutionsService solutionsService;

    @PostMapping("/add")
    public Result addSolutions(@RequestBody SolutionsVO solutionsVO) {

        return solutionsService.addSolutions(solutionsVO);

    }
}
