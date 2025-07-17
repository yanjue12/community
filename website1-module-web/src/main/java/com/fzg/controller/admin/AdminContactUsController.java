package com.fzg.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.model.ContactUs;
import com.fzg.model.Result;
import com.fzg.service.ContactUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/contactUs")
@RequiredArgsConstructor
public class AdminContactUsController {

    private final ContactUsService contactUsService;

    @GetMapping("/list")
    public Result listSolutions() {
        LambdaQueryWrapper<ContactUs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ContactUs::getCreatedAt);
        List<ContactUs> list = contactUsService.list(queryWrapper);
        return Result.success(list);
    }

    //修改状态
    @GetMapping("/changeState/{id}")
    public Result changeState(@PathVariable Integer id, @RequestParam Short state) {
        return contactUsService.changeState(id, state);
    }


}
