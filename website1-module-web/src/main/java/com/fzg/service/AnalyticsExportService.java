package com.fzg.service;

import javax.servlet.http.HttpServletResponse;

public interface AnalyticsExportService {

    /** 导出 Excel（多 Sheet） */
    void exportExcel(HttpServletResponse response);

    /** 导出 PDF */
    void exportPdf(HttpServletResponse response);
}
