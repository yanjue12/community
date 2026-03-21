package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.SystemConfigGroupMapper;
import com.fzg.model.SystemConfigGroup;
import com.fzg.service.SystemConfigGroupService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigGroupServiceImpl extends ServiceImpl<SystemConfigGroupMapper, SystemConfigGroup> implements SystemConfigGroupService {
}
