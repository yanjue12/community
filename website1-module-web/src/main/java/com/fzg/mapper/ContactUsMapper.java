package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.ContactUs;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yanju
* @description 针对表【contact_us(联系记录表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:28
* @Entity model.ContactUs
*/
@Mapper
public interface ContactUsMapper extends BaseMapper<ContactUs> {

}




