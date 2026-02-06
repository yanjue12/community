package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.converter.ArticleEsConverter;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.AuditLogMapper;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.model.Article;
import com.fzg.model.AuditLog;
import com.fzg.model.AuditRecord;
import com.fzg.service.ArticleTagService;
import com.fzg.service.AuditRecordService;
import com.fzg.service.SensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class AuditRecordServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditRecordService{


    @Autowired
    private Articlemapper articleMapper;
    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private SensitiveService sensitiveService;
    @Autowired
    private ArticleTagService articleTagService;

    /**
     * 创建审核记录
     * @param
     */
    @Override
    public void createAudit(Long articleId) {
        AuditRecord record = new AuditRecord();
        record.setBizType("ARTICLE");
        record.setArticleId(articleId);
        record.setAuditStatus((byte) 0); // 待审核
        record.setAuditType((byte) 1);   // 默认自动
        baseMapper.insert(record);
    }

    /**
     * 自动审核
     * @param article
     */
    @Override
    @Transactional
    public void autoAudit(Article article) {
        AuditRecord record = baseMapper.selectByArticleId(article.getId());
        if(record == null){
            throw new RuntimeException("审核记录不存在");
        }

        //1.敏感词检测
        String hitWord = sensitiveService.hit(article.getTitle() + article.getContent());
        if (hitWord != null) {
            doReject(record, article, "命中敏感词：" + hitWord, true);
            return;
        }

        //2.通过
        doPass(record, article, true);
    }

    private void doReject(AuditRecord record,
                          Article article,
                          String reason,
                          boolean auto) {

        record.setAuditStatus((byte) 2); // 拒绝
        record.setAuditType((byte) (auto ? 1 : 2));
        record.setReason(reason);
        record.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        baseMapper.updateById(record);

        article.setStatus("3"); // 审核拒绝
        articleMapper.updateById(article);

        auditLogMapper.insert(
                new AuditLog(
                        record.getId(),
                        auto ? (byte) 2 : (byte) 4,
                        null,
                        reason
                )
        );
    }




    private void doPass(AuditRecord record,
                        Article article,
                        boolean auto) {

        record.setAuditStatus((byte) 1); // 通过
        record.setAuditType((byte) (auto ? 1 : 2));
        record.setUpdatedAt(new Date());
        baseMapper.updateById(record);

        article.setStatus("1"); // 已发布
        articleMapper.updateById(article);

        auditLogMapper.insert(
                new AuditLog(
                        record.getId(),
                        auto ? (byte) 1 : (byte) 3,
                        null,
                        "通过"
                )
        );

        List<String> tags = articleTagService.listTagNamesByArticleId(article.getId());

        // 同步 ES（唯一入口）
        elasticsearchRestTemplate.save(ArticleEsConverter.toEs(article, tags));
    }


}
