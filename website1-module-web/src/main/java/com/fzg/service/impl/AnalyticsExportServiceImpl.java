package com.fzg.service.impl;

import com.fzg.dto.analytics.ChartDataDTO;
import com.fzg.dto.analytics.DashboardDTO;
import com.fzg.service.AnalyticsExportService;
import com.fzg.service.AnalyticsService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class AnalyticsExportServiceImpl implements AnalyticsExportService {

    private final AnalyticsService analyticsService;
    private final Executor exportExecutor;

    public AnalyticsExportServiceImpl(AnalyticsService analyticsService,
                                      @Qualifier("exportExecutor") Executor exportExecutor) {
        this.analyticsService = analyticsService;
        this.exportExecutor = exportExecutor;
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String REPORT_TITLE = "管理端数据分析报告";

    // ===================== 并行数据收集 =====================

    /** 所有导出所需数据，并行拉取 */
    private static class ExportData {
        DashboardDTO dashboard;
        List<ChartDataDTO.LineItem> userTrend;
        List<ChartDataDTO.LineItem> articleTrend;
        List<ChartDataDTO.RankItem> hotArticles;
        List<ChartDataDTO.RankItem> activeUsers;
        List<ChartDataDTO.PieItem> categories;
        List<ChartDataDTO.PieItem> tags;
    }

    private ExportData fetchAllParallel() {
        CompletableFuture<DashboardDTO> f1 = CompletableFuture.supplyAsync(
                analyticsService::getDashboardData, exportExecutor);
        CompletableFuture<List<ChartDataDTO.LineItem>> f2 = CompletableFuture.supplyAsync(
                () -> analyticsService.getUserGrowthTrend("thisMonth", null, null), exportExecutor);
        CompletableFuture<List<ChartDataDTO.LineItem>> f3 = CompletableFuture.supplyAsync(
                () -> analyticsService.getArticlePublishTrend("thisMonth", null, null), exportExecutor);
        CompletableFuture<List<ChartDataDTO.RankItem>> f4 = CompletableFuture.supplyAsync(
                () -> analyticsService.getHotArticles(10, 30), exportExecutor);
        CompletableFuture<List<ChartDataDTO.RankItem>> f5 = CompletableFuture.supplyAsync(
                () -> analyticsService.getActiveUsers(10, 7), exportExecutor);
        CompletableFuture<List<ChartDataDTO.PieItem>> f6 = CompletableFuture.supplyAsync(
                analyticsService::getCategoryDistribution, exportExecutor);
        CompletableFuture<List<ChartDataDTO.PieItem>> f7 = CompletableFuture.supplyAsync(
                () -> analyticsService.getTagDistribution(20), exportExecutor);

        // 等全部完成
        CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f7).join();

        ExportData data = new ExportData();
        data.dashboard    = f1.join();
        data.userTrend    = f2.join();
        data.articleTrend = f3.join();
        data.hotArticles  = f4.join();
        data.activeUsers  = f5.join();
        data.categories   = f6.join();
        data.tags         = f7.join();
        return data;
    }

    // ===================== Excel =====================

    @Override
    public void exportExcel(HttpServletResponse response) {
        try {
            long t0 = System.currentTimeMillis();
            ExportData data = fetchAllParallel();
            log.info("导出数据并行拉取耗时: {}ms", System.currentTimeMillis() - t0);

            XSSFWorkbook workbook = new XSSFWorkbook();
            buildOverviewSheet(workbook, data.dashboard.getOverview());
            buildTrendSheet(workbook, "用户增长趋势(本月)", data.userTrend, "日期", "新增用户数");
            buildTrendSheet(workbook, "文章发布趋势(本月)", data.articleTrend, "日期", "发布文章数");
            buildArticleRankSheet(workbook, data.hotArticles);
            buildUserRankSheet(workbook, data.activeUsers);
            buildPieSheet(workbook, "文章分类分布", data.categories, "分类名称", "文章数量");
            buildPieSheet(workbook, "标签使用分布", data.tags, "标签名称", "使用次数");

            String filename = URLEncoder.encode(REPORT_TITLE + "_" + LocalDate.now().format(DATE_FMT) + ".xlsx",
                    StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            workbook.write(response.getOutputStream());
            workbook.close();
            log.info("Excel 导出总耗时: {}ms", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("导出 Excel 失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    private XSSFCellStyle titleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new Color(63, 114, 175), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(new XSSFColor(Color.WHITE, null));
        style.setFont(titleFont);
        return style;
    }

    private XSSFCellStyle headerStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new Color(220, 230, 241), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle dataStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void setCell(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val == null ? "" : val);
        if (style != null) cell.setCellStyle(style);
    }

    private void setCell(Row row, int col, long val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        if (style != null) cell.setCellStyle(style);
    }

    private void buildOverviewSheet(XSSFWorkbook wb, DashboardDTO.OverviewData ov) {
        XSSFSheet sheet = wb.createSheet("概览摘要");
        sheet.setColumnWidth(0, 7000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 5000);

        XSSFCellStyle titleSt = titleStyle(wb);
        XSSFCellStyle headerSt = headerStyle(wb);
        XSSFCellStyle dataSt = dataStyle(wb);

        // 大标题
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(28);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(REPORT_TITLE + " - 概览摘要");
        titleCell.setCellStyle(titleSt);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 生成时间
        Row genRow = sheet.createRow(1);
        setCell(genRow, 0, "生成时间：" + LocalDate.now().format(DATE_FMT), null);

        // 空行
        sheet.createRow(2);

        // 表头
        Row header = sheet.createRow(3);
        String[] headers = {"指标", "今日", "本月", "上月", "总计"};
        for (int i = 0; i < headers.length; i++) setCell(header, i, headers[i], headerSt);

        if (ov == null) return;

        String[][] rows = {
            {"新增用户数",
                str(ov.getTodayUsers()), str(ov.getCurrentMonthUsers()),
                str(ov.getLastMonthUsers()), str(ov.getTotalUsers())},
            {"新增文章数",
                str(ov.getTodayArticles()), str(ov.getCurrentMonthArticles()),
                str(ov.getLastMonthArticles()), str(ov.getTotalArticles())},
            {"新增评论数",
                str(ov.getTodayComments()), str(ov.getCurrentMonthComments()),
                str(ov.getLastMonthComments()), str(ov.getTotalComments())},
            {"浏览量",
                str(ov.getTodayViews()), str(ov.getCurrentMonthViews()),
                str(ov.getLastMonthViews()), str(ov.getTotalViews())},
        };

        for (int r = 0; r < rows.length; r++) {
            Row row = sheet.createRow(4 + r);
            for (int c = 0; c < rows[r].length; c++) setCell(row, c, rows[r][c], dataSt);
        }

        // 增长率
        sheet.createRow(9);
        Row rateHeader = sheet.createRow(10);
        setCell(rateHeader, 0, "增长率（本月 vs 上月）", headerSt);
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 4));

        Row rateRow = sheet.createRow(11);
        String[] rateHeaders = {"用户增长率", "文章增长率", "评论增长率", "浏览量增长率", ""};
        for (int i = 0; i < 4; i++) setCell(rateRow, i, rateHeaders[i], headerSt);

        Row rateData = sheet.createRow(12);
        setCell(rateData, 0, pct(ov.getUserGrowthRate()), dataSt);
        setCell(rateData, 1, pct(ov.getArticleGrowthRate()), dataSt);
        setCell(rateData, 2, pct(ov.getCommentGrowthRate()), dataSt);
        setCell(rateData, 3, pct(ov.getViewGrowthRate()), dataSt);
    }

    private void buildTrendSheet(XSSFWorkbook wb, String sheetName,
                                  List<ChartDataDTO.LineItem> items, String col1, String col2) {
        XSSFSheet sheet = wb.createSheet(sheetName);
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);

        XSSFCellStyle headerSt = headerStyle(wb);
        XSSFCellStyle dataSt = dataStyle(wb);

        Row header = sheet.createRow(0);
        setCell(header, 0, col1, headerSt);
        setCell(header, 1, col2, headerSt);

        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            ChartDataDTO.LineItem item = items.get(i);
            Row row = sheet.createRow(i + 1);
            setCell(row, 0, item.getDate(), dataSt);
            setCell(row, 1, item.getValue() == null ? 0 : item.getValue(), dataSt);
        }
    }

    private void buildArticleRankSheet(XSSFWorkbook wb, List<ChartDataDTO.RankItem> items) {
        XSSFSheet sheet = wb.createSheet("热门文章排行(近30天)");
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(2, 4000);

        XSSFCellStyle headerSt = headerStyle(wb);
        XSSFCellStyle dataSt = dataStyle(wb);

        Row header = sheet.createRow(0);
        setCell(header, 0, "排名", headerSt);
        setCell(header, 1, "文章标题", headerSt);
        setCell(header, 2, "浏览量", headerSt);

        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            ChartDataDTO.RankItem item = items.get(i);
            Row row = sheet.createRow(i + 1);
            setCell(row, 0, i + 1, dataSt);
            setCell(row, 1, item.getTitle(), dataSt);
            setCell(row, 2, item.getValue() == null ? 0 : item.getValue(), dataSt);
        }
    }

    private void buildUserRankSheet(XSSFWorkbook wb, List<ChartDataDTO.RankItem> items) {
        XSSFSheet sheet = wb.createSheet("活跃用户排行(近7天)");
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 4000);

        XSSFCellStyle headerSt = headerStyle(wb);
        XSSFCellStyle dataSt = dataStyle(wb);

        Row header = sheet.createRow(0);
        setCell(header, 0, "排名", headerSt);
        setCell(header, 1, "用户名", headerSt);
        setCell(header, 2, "活跃分值", headerSt);

        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            ChartDataDTO.RankItem item = items.get(i);
            Row row = sheet.createRow(i + 1);
            setCell(row, 0, i + 1, dataSt);
            setCell(row, 1, item.getTitle(), dataSt);
            setCell(row, 2, item.getValue() == null ? 0 : item.getValue(), dataSt);
        }
    }

    private void buildPieSheet(XSSFWorkbook wb, String sheetName,
                                List<ChartDataDTO.PieItem> items, String col1, String col2) {
        XSSFSheet sheet = wb.createSheet(sheetName);
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);

        XSSFCellStyle headerSt = headerStyle(wb);
        XSSFCellStyle dataSt = dataStyle(wb);

        Row header = sheet.createRow(0);
        setCell(header, 0, col1, headerSt);
        setCell(header, 1, col2, headerSt);

        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            ChartDataDTO.PieItem item = items.get(i);
            Row row = sheet.createRow(i + 1);
            setCell(row, 0, item.getName(), dataSt);
            setCell(row, 1, item.getValue() == null ? 0 : item.getValue(), dataSt);
        }
    }

    // ===================== PDF =====================

    @Override
    public void exportPdf(HttpServletResponse response) {
        try {
            long t0 = System.currentTimeMillis();
            ExportData data = fetchAllParallel();
            log.info("PDF 导出数据并行拉取耗时: {}ms", System.currentTimeMillis() - t0);

            DashboardDTO dashboard = data.dashboard;
            List<ChartDataDTO.LineItem> userTrend = data.userTrend;
            List<ChartDataDTO.LineItem> articleTrend = data.articleTrend;
            List<ChartDataDTO.RankItem> hotArticles = data.hotArticles;
            List<ChartDataDTO.RankItem> activeUsers = data.activeUsers;
            List<ChartDataDTO.PieItem> categories = data.categories;

            String filename = URLEncoder.encode(REPORT_TITLE + "_" + LocalDate.now().format(DATE_FMT) + ".pdf",
                    StandardCharsets.UTF_8.name());
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, response.getOutputStream());
            doc.open();

            // 中文字体（使用内置 CJK 字体）
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD, Color.decode("#1a3a5c"));
            Font sectionFont = new Font(bf, 13, Font.BOLD, Color.decode("#3f72af"));
            Font headerFont = new Font(bf, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(bf, 9, Font.NORMAL, Color.BLACK);
            Font smallFont = new Font(bf, 8, Font.NORMAL, Color.GRAY);

            // 主标题
            Paragraph title = new Paragraph(REPORT_TITLE, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            doc.add(title);

            Paragraph genTime = new Paragraph("生成时间：" + LocalDate.now().format(DATE_FMT), smallFont);
            genTime.setAlignment(Element.ALIGN_CENTER);
            genTime.setSpacingAfter(16);
            doc.add(genTime);

            // 1. 概览摘要
            addPdfSection(doc, "一、概览摘要", sectionFont);
            DashboardDTO.OverviewData ov = dashboard.getOverview();
            if (ov != null) {
                PdfPTable ovTable = new PdfPTable(5);
                ovTable.setWidthPercentage(100);
                ovTable.setWidths(new float[]{3f, 2f, 2f, 2f, 2f});
                addPdfTableHeader(ovTable, headerFont, "指标", "今日", "本月", "上月", "总计");
                addPdfTableRow(ovTable, bodyFont, "新增用户数", str(ov.getTodayUsers()), str(ov.getCurrentMonthUsers()), str(ov.getLastMonthUsers()), str(ov.getTotalUsers()));
                addPdfTableRow(ovTable, bodyFont, "新增文章数", str(ov.getTodayArticles()), str(ov.getCurrentMonthArticles()), str(ov.getLastMonthArticles()), str(ov.getTotalArticles()));
                addPdfTableRow(ovTable, bodyFont, "新增评论数", str(ov.getTodayComments()), str(ov.getCurrentMonthComments()), str(ov.getLastMonthComments()), str(ov.getTotalComments()));
                addPdfTableRow(ovTable, bodyFont, "浏览量", str(ov.getTodayViews()), str(ov.getCurrentMonthViews()), str(ov.getLastMonthViews()), str(ov.getTotalViews()));
                ovTable.setSpacingAfter(8);
                doc.add(ovTable);

                // 增长率小表
                PdfPTable rateTable = new PdfPTable(4);
                rateTable.setWidthPercentage(100);
                addPdfTableHeader(rateTable, headerFont, "用户增长率", "文章增长率", "评论增长率", "浏览量增长率");
                addPdfTableRow(rateTable, bodyFont, pct(ov.getUserGrowthRate()), pct(ov.getArticleGrowthRate()), pct(ov.getCommentGrowthRate()), pct(ov.getViewGrowthRate()));
                rateTable.setSpacingAfter(16);
                doc.add(rateTable);
            }

            // 2. 用户增长趋势
            addPdfSection(doc, "二、用户增长趋势（本月）", sectionFont);
            doc.add(buildPdfTrendTable(userTrend, "日期", "新增用户数", bodyFont, headerFont));

            // 3. 文章发布趋势
            addPdfSection(doc, "三、文章发布趋势（本月）", sectionFont);
            doc.add(buildPdfTrendTable(articleTrend, "日期", "发布文章数", bodyFont, headerFont));

            // 4. 热门文章排行
            addPdfSection(doc, "四、热门文章排行（近30天 Top10）", sectionFont);
            if (hotArticles != null && !hotArticles.isEmpty()) {
                PdfPTable t = new PdfPTable(3);
                t.setWidthPercentage(100);
                t.setWidths(new float[]{1f, 6f, 2f});
                addPdfTableHeader(t, headerFont, "排名", "文章标题", "浏览量");
                for (int i = 0; i < hotArticles.size(); i++) {
                    ChartDataDTO.RankItem item = hotArticles.get(i);
                    addPdfTableRow(t, bodyFont, String.valueOf(i + 1), item.getTitle(), str(item.getValue()));
                }
                t.setSpacingAfter(16);
                doc.add(t);
            }

            // 5. 活跃用户排行
            addPdfSection(doc, "五、活跃用户排行（近7天 Top10）", sectionFont);
            if (activeUsers != null && !activeUsers.isEmpty()) {
                PdfPTable t = new PdfPTable(3);
                t.setWidthPercentage(100);
                t.setWidths(new float[]{1f, 5f, 2f});
                addPdfTableHeader(t, headerFont, "排名", "用户名", "活跃分值");
                for (int i = 0; i < activeUsers.size(); i++) {
                    ChartDataDTO.RankItem item = activeUsers.get(i);
                    addPdfTableRow(t, bodyFont, String.valueOf(i + 1), item.getTitle(), str(item.getValue()));
                }
                t.setSpacingAfter(16);
                doc.add(t);
            }

            // 6. 分类分布
            addPdfSection(doc, "六、文章分类分布", sectionFont);
            if (categories != null && !categories.isEmpty()) {
                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(60);
                t.setHorizontalAlignment(Element.ALIGN_LEFT);
                addPdfTableHeader(t, headerFont, "分类名称", "文章数量");
                for (ChartDataDTO.PieItem item : categories) {
                    addPdfTableRow(t, bodyFont, item.getName(), str(item.getValue()));
                }
                t.setSpacingAfter(16);
                doc.add(t);
            }

            doc.close();
        } catch (Exception e) {
            log.error("导出 PDF 失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出 PDF 失败", e);
        }
    }

    private void addPdfSection(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private PdfPTable buildPdfTrendTable(List<ChartDataDTO.LineItem> items,
                                          String col1, String col2, Font bodyFont, Font headerFont) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(60);
        t.setHorizontalAlignment(Element.ALIGN_LEFT);
        addPdfTableHeader(t, headerFont, col1, col2);
        if (items != null) {
            for (ChartDataDTO.LineItem item : items) {
                addPdfTableRow(t, bodyFont, item.getDate(), str(item.getValue()));
            }
        }
        t.setSpacingAfter(16);
        return t;
    }

    private void addPdfTableHeader(PdfPTable table, Font font, String... headers) {
        Color headerBg = Color.decode("#3f72af");
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addPdfTableRow(PdfPTable table, Font font, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : v, font));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    // ===================== 工具方法 =====================

    private String str(Long val) {
        return val == null ? "0" : String.valueOf(val);
    }

    private String str(long val) {
        return String.valueOf(val);
    }

    private String pct(java.math.BigDecimal val) {
        if (val == null) return "0%";
        return val.setScale(2, java.math.RoundingMode.HALF_UP) + "%";
    }
}
