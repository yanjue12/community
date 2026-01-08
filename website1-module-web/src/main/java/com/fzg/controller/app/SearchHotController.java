package com.fzg.controller.app;

import com.fzg.model.SearchHot;
import com.fzg.service.impl.SearchHotServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchHotController {

    @Autowired
    private SearchHotServiceImpl searchHotService;

    @PostMapping("/searchHot")
    public List<SearchHot> searchHot(){
        List<SearchHot> searchHots = searchHotService.selectList();
        return searchHots;
    }
}
