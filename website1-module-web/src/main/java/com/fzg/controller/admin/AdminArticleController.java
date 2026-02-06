package com.fzg.controller.admin;

import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import com.fzg.service.AuditRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/article")
@RestController
public class AdminArticleController {

    @Autowired
    private Articlemapper articleMapper;
    @Autowired
    private AuditRecordMapper auditRecordMapper;
    @Autowired
    private AuditRecordService auditRecordService;

    @PostMapping("/audit/manual/pass")
    public Result manualPass(Long articleId, Long adminId) {

        return null;
    }



}
