package com.fzg.controller.admin;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.annotation.OperationLogAnnotation;
import com.fzg.enums.EnumReturn;
import com.fzg.model.ContactUs;
import com.fzg.model.Result;
import com.fzg.service.ContactUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/contactUs")
@RequiredArgsConstructor
@SaCheckLogin
public class AdminContactUsController {

    private final ContactUsService contactUsService;

    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result listSolutions() {

        if(!StpUtil.isLogin()){
            return Result.fail(EnumReturn.UN_LOGIN_OR_TOKEN_INVAID);
        }
        LambdaQueryWrapper<ContactUs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ContactUs::getCreatedAt);
        List<ContactUs> list = contactUsService.list(queryWrapper);
        return Result.success(list);
    }


    @PutMapping("/changeState/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "修改联系记录状态",operationType = "Put-changeState")
    public Result changeState(@PathVariable Integer id, @RequestParam Short state) {
        return contactUsService.changeState(id, state);
    }


    @DeleteMapping("/delete/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "删除联系记录", operationType = "DELETE-delete-id")
    public Result deleteContactUs(@PathVariable Integer id) {
        try {
            // 调用服务层方法删除联系记录
            boolean deleteResult = contactUsService.removeById(id);
            if (deleteResult) {
                return Result.success(EnumReturn.OPERATION_SUCCESS);
            } else {
                return Result.fail(EnumReturn.OPERATION_FAIL);
            }
        } catch (Exception e) {
            // 处理异常
            return Result.fail(EnumReturn.OPERATION_FAIL);
        }
    }



    @GetMapping("/search")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationDesc = "根据姓名模糊查询联系记录", operationType = "Get-search")
    public Result searchByContactName(@RequestParam String name) {

        LambdaQueryWrapper<ContactUs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ContactUs::getName, name);
        queryWrapper.orderByDesc(ContactUs::getCreatedAt);
        List<ContactUs> list = contactUsService.list(queryWrapper);
        return Result.success(list);
    }



}
