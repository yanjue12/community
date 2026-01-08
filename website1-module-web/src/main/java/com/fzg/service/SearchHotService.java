package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.SearchHot;

import java.util.List;

public interface SearchHotService extends IService<SearchHot> {

    List<SearchHot> selectList();
}
