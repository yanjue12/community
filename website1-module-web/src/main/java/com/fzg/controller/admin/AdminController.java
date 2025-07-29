package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@SaCheckLogin
public class AdminController {

    /*@PostMapping("/add")
    public Result addAdmin(@RequestBody){
        return
    }*/
}
