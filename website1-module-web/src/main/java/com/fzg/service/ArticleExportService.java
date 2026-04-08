package com.fzg.service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ArticleExportService {

    /**
     * 导出文章统计及明细到 Excel
     *
     * @param start 开始日期（含），为空默认本周一
     * @param end   结束日期（含），为空默认今天
     * @param response HTTP 响应流
     */
    void exportExcel(LocalDate start, LocalDate end, HttpServletResponse response);

    /**
     * 导出文章统计及明细到 PDF
     */
    void exportPdf(LocalDate start, LocalDate end, HttpServletResponse response);
}
