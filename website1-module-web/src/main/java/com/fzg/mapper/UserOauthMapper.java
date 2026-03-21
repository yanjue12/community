package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.UserOauth;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOauthMapper extends BaseMapper<UserOauth> {


    @Select("select * from user_oauth where oauth_type = #{type} and open_id = #{openId}")
    UserOauth selectByOpenId(@Param("type") String type, @Param("openId") String openId);

}
