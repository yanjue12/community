package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.model.ContactUs;
import com.fzg.service.ContactUsService;
import com.fzg.mapper.ContactUsMapper;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【contact_us(联系记录表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:28
*/
@Service
public class ContactUsServiceImpl extends ServiceImpl<ContactUsMapper, ContactUs>
    implements ContactUsService{

}




