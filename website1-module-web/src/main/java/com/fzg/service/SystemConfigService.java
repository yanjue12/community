package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {

    /** 根据 configKey 获取配置值 */
    String getValueByKey(String configKey);
}
