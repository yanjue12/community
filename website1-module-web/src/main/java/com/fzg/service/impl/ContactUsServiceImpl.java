package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.model.ContactUs;
import com.fzg.model.Result;
import com.fzg.service.ContactUsService;
import com.fzg.mapper.ContactUsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【contact_us(联系记录表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:28
*/
@Service
@RequiredArgsConstructor
public class ContactUsServiceImpl extends ServiceImpl<ContactUsMapper, ContactUs>
    implements ContactUsService{

    private final ContactUsMapper contactUsMapper;

    @Override
    public Result mySave(ContactUs contactUs) {

        if(contactUs.getEmail()==null){
            return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
        }

        if(contactUs.getMessage()==null) {
            return Result.fail(EnumReturn.CONTENT_NOT_EXISTS);
        }
        if(contactUs.getName()==null) {
            return Result.fail(EnumReturn.NAME_NOT_EXISTS);
        }

        if(contactUsMapper.insert(contactUs) == 0) {
            return Result.fail(EnumReturn.OPERATION_FAIL);
        }else{
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        }
    }
}




