package com.fzg.service.impl;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.SearchHotmapper;
import com.fzg.model.SearchHot;
import com.fzg.service.SearchHotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SearchHotServiceImpl extends ServiceImpl<SearchHotmapper,SearchHot> implements SearchHotService {

    @Autowired
    private SearchHotmapper searchHotMapper;

    @Override
    public List<SearchHot> selectList() {
        //查点击量前十的数据
        List<SearchHot> list = searchHotMapper.queryHot();
        log.info("查询成功数据：{}", JSON.toJSON(list));
        return list;
    }
}
