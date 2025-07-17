package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.ContactUs;
import com.fzg.model.Result;

/**
* @author yanju
* @description 针对表【contact_us(联系记录表)】的数据库操作Service
* @createDate 2025-07-09 17:09:28
*/
public interface ContactUsService extends IService<ContactUs> {

    Result mySave(ContactUs contactUs);

    Result changeState(Integer id, Short state);
}
