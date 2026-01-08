package com.fzg.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.model.Result;
import com.fzg.service.SolutionsService;
import com.fzg.vo.SolutionsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/solutions")
@RequiredArgsConstructor
@Schema(description = "解决方案接口")
public class SolutionsController {

    private final SolutionsService solutionsService;

    @GetMapping("/list")
    public Result<Page<SolutionsVO>> solutionsList(
            @RequestParam(defaultValue = "1") Integer pageNumber) {

        Page<SolutionsVO> solutionsVOList =  solutionsService.solutionsList(pageNumber,9);

        return Result.success(solutionsVOList);
    }



}


