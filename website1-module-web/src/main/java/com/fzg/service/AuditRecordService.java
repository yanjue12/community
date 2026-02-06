package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;

public interface AuditRecordService extends IService<AuditRecord> {
    void createAudit(Long id);

    void autoAudit(Article articleVO);
}
