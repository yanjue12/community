package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Category;
import com.fzg.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<Categorymapper, Category> implements CategoryService {
}
