package com.fzg.controller.app;


import com.fzg.model.ContactUs;
import com.fzg.model.Result;
import com.fzg.service.ContactUsService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/contactUs")
@Schema(description = "联系我们接口")
@RequiredArgsConstructor
@Log4j2
public class ContactUsController {

    private final ContactUsService contactUsService;


    @PostMapping("/submit")
    public Result submit(@RequestBody ContactUs contactUs) {
        return contactUsService.mySave(contactUs);
    }

}
