package com.fzg.controller.app;

import com.fzg.model.Result;
import com.fzg.model.Solutions;
import com.fzg.service.SolutionsService;
import com.fzg.vo.SolutionsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app/solutions")
@RequiredArgsConstructor
@Schema(description = "解决方案接口")
public class SolutionsController {

    private final SolutionsService solutionsService;

    @GetMapping("/list")
    public Result<List<SolutionsVO>> solutionsList() {

        //获取解决方案列表（包含子标题表获取）
        List<SolutionsVO> solutionsVOList =  solutionsService.solutionsList();

        return Result.success(solutionsVOList);
    }

}


