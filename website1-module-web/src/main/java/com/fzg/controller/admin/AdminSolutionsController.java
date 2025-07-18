package com.fzg.controller.admin;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.fzg.annotation.OperationLogAnnotation;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.SolutionsService;
import com.fzg.vo.SolutionsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员解决方案管理接口
 */
@RestController
@RequestMapping("/admin/solutions")
@Schema(description = "管理员解决方案管理接口")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
public class AdminSolutionsController {

    private final SolutionsService solutionsService;

    //获取解决方案列表
    @GetMapping("/list")
    //@OperationLogAnnotation(operationDesc = "展示解决方案",operationType = "Get-list")
    @SaCheckRole("admin")
    public Result listSolutions() {

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        log.info("tokenInfo:{}", tokenInfo);

        if(tokenInfo.getTokenValue() == null || !StpUtil.isLogin()){
            return Result.fail(EnumReturn.UN_LOGIN_OR_TOKEN_INVAID);
        }

        return solutionsService.AdminSolutionsList();
    }



    @PostMapping("/add")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "创建解决方案",operationType = "POST-add")
    public Result addSolutions(@RequestBody SolutionsVO solutionsVO) {

        return solutionsService.addSolutions(solutionsVO);
    }



    //修改解决方案
    @PutMapping("/update/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "修改解决方案",operationType = "PUT-update")
    public Result updateSolutions(@PathVariable Integer id, @RequestBody SolutionsVO solutionsVO) {
        log.info("修改解决方案，ID：{}，数据：{}", id, solutionsVO);
        return solutionsService.updateSolutions(id, solutionsVO);
    }

    /**
     *删除解决方案
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "删除解决方案",operationType = "DELETE-delete-id")
    public Result deleteSolutions(@PathVariable Integer id) {
        return solutionsService.deleteSolutions(id);
    }


    //批量删除解决方案
    @DeleteMapping("/batchDelete")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "批量删除解决方案",operationType = "DELETE-batchDelete")
    public Result batchDeleteSolutions(@RequestBody List<Integer> ids) {
        return solutionsService.batchDeleteSolutions(ids);
    }


    // 修改解决方案状态
    @PutMapping("/changeState/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "修改解决方案状态",operationType = "PUT-changeState")
    public Result changeSolutionsState(@PathVariable Integer id, @RequestParam Short state) {
        return solutionsService.changeSolutionsState(id, state);
    }



}
