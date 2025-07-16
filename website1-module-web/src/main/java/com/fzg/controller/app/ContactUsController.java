package com.fzg.controller.app;


import com.fzg.model.ContactUs;
import com.fzg.model.News;
import com.fzg.model.Result;
import com.fzg.service.ContactUsService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/contactUs")
@Schema(description = "联系我们接口")
@RequiredArgsConstructor
public class ContactUsController {

    private final ContactUsService contactUsService;


    @PostMapping("/submit")
    public Result submit(@RequestBody ContactUs contactUs) {

        Result r = contactUsService.mySave(contactUs);

        return null;
    }

}
