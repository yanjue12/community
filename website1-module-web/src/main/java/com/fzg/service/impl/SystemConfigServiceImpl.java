package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.SystemConfigMapper;
import com.fzg.model.SystemConfig;
import com.fzg.service.SystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getValueByKey(String configKey) {
        SystemConfig config = baseMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .eq(SystemConfig::getStatus, "1")
        );
        return config != null ? config.getConfigValue() : null;
    }
}
