package com.fzg.mapper;

import com.fzg.model.Notification;
import org.springframework.stereotype.Repository;

@Repository
public interface Notificationmapper {
    int deleteByPrimaryKey(Long id);

    int insert(Notification record);

    int insertSelective(Notification record);

    Notification selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Notification record);

    int updateByPrimaryKey(Notification record);
}