package com.fzg.service.impl;

import com.fzg.mapper.Articlemapper;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import com.fzg.service.ArticleExportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleExportServiceImpl implements ArticleExportService {
    private final Articlemapper articleMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void exportExcel(LocalDate start, LocalDate end, HttpServletResponse response) {
        try {
            if (start == null || end == null) {
                // 默认本周
                LocalDate today = LocalDate.now();
                DayOfWeek firstDay = DayOfWeek.MONDAY;
                start = today.with(firstDay);
                end = today;
            }
            ArticleRequest req = new ArticleRequest();
            req.setStartTime(start.format(DATE_FMT));
            req.setEndTime(end.format(DATE_FMT));
            req.setPageSize(10000); // 导出上限 1w
            req.setPageNum(1);
            List<ArticleVO> list = articleMapper.queryArticleByCondition(req, 0);

            // 统计
            long total = list.size();
            long published = list.stream().filter(v -> "1".equals(v.getStatus())).count();
            long pending = list.stream().filter(v -> "2".equals(v.getStatus())).count();
            long views = list.stream().mapToLong(v -> v.getViewCount() == null ? 0 : v.getViewCount()).sum();
            long likes = list.stream().mapToLong(v -> v.getLikeCount() == null ? 0 : v.getLikeCount()).sum();

            XSSFWorkbook wb = new XSSFWorkbook();
            Sheet stat = wb.createSheet("统计信息");
            String[][] statRows = {
                    {"导出范围", start.format(DATE_FMT) + " 至 " + end.format(DATE_FMT)},
                    {"文章总数", String.valueOf(total)},
                    {"已发布", String.valueOf(published)},
                    {"待审核", String.valueOf(pending)},
                    {"总浏览量", String.valueOf(views)},
                    {"总点赞数", String.valueOf(likes)}
            };
            int r = 0;
            for (String[] rowData : statRows) {
                Row row = stat.createRow(r++);
                row.createCell(0).setCellValue(rowData[0]);
                row.createCell(1).setCellValue(rowData[1]);
            }

            // 文章明细
            Sheet detail = wb.createSheet("文章明细");
            String[] headers = {"ID", "标题", "摘要", "作者昵称", "用户ID", "分类", "标签", "状态", "浏览", "点赞", "评论", "收藏", "分享", "发布时间"};
            Row header = detail.createRow(0);
            CellStyle headSt = wb.createCellStyle();
            XSSFCellStyle xst = (XSSFCellStyle) headSt;
            XSSFFont font = wb.createFont();
            font.setBold(true);
            headSt.setFont(font);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headSt);
            }
            int rowIdx = 1;
            for (ArticleVO vo : list) {
                Row row = detail.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(vo.getId());
                row.createCell(c++).setCellValue(safe(vo.getTitle()));
                row.createCell(c++).setCellValue(safe(vo.getSummary()));
                row.createCell(c++).setCellValue(safe(vo.getNickName()));
                row.createCell(c++).setCellValue(vo.getUserId() == null ? 0 : vo.getUserId());
                row.createCell(c++).setCellValue(safe(vo.getCategoryName()));
                row.createCell(c++).setCellValue(safe(vo.getTagName()));
                row.createCell(c++).setCellValue(safe(vo.getStatus()));
                row.createCell(c++).setCellValue(vo.getViewCount() == null ? 0 : vo.getViewCount());
                row.createCell(c++).setCellValue(vo.getLikeCount() == null ? 0 : vo.getLikeCount());
                row.createCell(c++).setCellValue(vo.getCommentCount() == null ? 0 : vo.getCommentCount());
                row.createCell(c++).setCellValue(vo.getCollectCount() == null ? 0 : vo.getCollectCount());
                row.createCell(c++).setCellValue(vo.getShareCount() == null ? 0 : vo.getShareCount());
                row.createCell(c++).setCellValue(vo.getPublishedAt() == null ? "" : DATE_FMT.format(vo.getPublishedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()));
            }

            String filename = URLEncoder.encode("文章导出_" + end.format(DATE_FMT) + ".xlsx", StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            log.error("导出 Excel 失败", e);
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    @Override
    public void exportPdf(LocalDate start, LocalDate end, HttpServletResponse response) {
        try {
            if (start == null || end == null) {
                LocalDate today = LocalDate.now();
                start = today.with(java.time.DayOfWeek.MONDAY);
                end = today;
            }
            ArticleRequest req = new ArticleRequest();
            req.setStartTime(start.format(DATE_FMT));
            req.setEndTime(end.format(DATE_FMT));
            req.setPageSize(10000);
            req.setPageNum(1);
            List<ArticleVO> list = articleMapper.queryArticleByCondition(req, 0);

            // 统计
            long total = list.size();
            long published = list.stream().filter(v -> "1".equals(v.getStatus())).count();
            long pending = list.stream().filter(v -> "2".equals(v.getStatus())).count();
            long views = list.stream().mapToLong(v -> v.getViewCount() == null ? 0 : v.getViewCount()).sum();
            long likes = list.stream().mapToLong(v -> v.getLikeCount() == null ? 0 : v.getLikeCount()).sum();

            String filename = URLEncoder.encode("文章导出_" + end.format(DATE_FMT) + ".pdf", StandardCharsets.UTF_8.name());
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, response.getOutputStream());
            doc.open();
            // 中文字体
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new  Font(bf, 16, Font.BOLD);
            Font headerFont = new Font(bf, 10, Font.BOLD);
            Font bodyFont = new  Font(bf, 9, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph("文章统计与明细导出", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            doc.add(title);

            Paragraph period = new Paragraph("时间范围：" + start + " 至 " + end,  bodyFont);
            period.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            doc.add(period);

            // 统计表
            PdfPTable stat = new PdfPTable(2);
            stat.setWidths(new float[]{3f, 2f});
            stat.setWidthPercentage(60);
            addCell(stat, "指标", headerFont, true);
            addCell(stat, "值", headerFont, true);
            addCell(stat, "文章总数", bodyFont, false);
            addCell(stat, String.valueOf(total), bodyFont, false);
            addCell(stat, "已发布", bodyFont, false);
            addCell(stat, String.valueOf(published), bodyFont, false);
            addCell(stat, "待审核", bodyFont, false);
            addCell(stat, String.valueOf(pending), bodyFont, false);
            addCell(stat, "总浏览量", bodyFont, false);
            addCell(stat, String.valueOf(views), bodyFont, false);
            addCell(stat, "总点赞数", bodyFont, false);
            addCell(stat, String.valueOf(likes), bodyFont, false);
            stat.setSpacingAfter(20);
            doc.add(stat);

            // 明细表
            PdfPTable detail = new PdfPTable(13);
            detail.setWidths(new float[]{2f, 6f, 4f, 3f, 4f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 4f});
            detail.setWidthPercentage(100);
            String[] headers = {"ID", "标题", "摘要", "作者昵称", "用户ID", "分类", "标签", "状态", "浏览", "点赞", "评论", "收藏", "分享", "发布时间"};
            for (String h : headers) addCell(detail, h, headerFont, true);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (ArticleVO vo : list) {
                addCell(detail, String.valueOf(vo.getId()), bodyFont, false);
                addCell(detail, safe(vo.getTitle()), bodyFont, false);
                addCell(detail, safe(vo.getSummary()), bodyFont, false);
                addCell(detail, safe(vo.getNickName()), bodyFont, false);
                addCell(detail, String.valueOf(vo.getUserId() == null ? 0 : vo.getUserId()), bodyFont, false);
                addCell(detail, safe(vo.getCategoryName()), bodyFont, false);
                addCell(detail, safe(vo.getTagName()), bodyFont, false);
                addCell(detail, safe(vo.getStatus()), bodyFont, false);
                addCell(detail, String.valueOf(vo.getViewCount() == null ? 0 : vo.getViewCount()), bodyFont, false);
                addCell(detail, String.valueOf(vo.getLikeCount() == null ? 0 : vo.getLikeCount()), bodyFont, false);
                addCell(detail, String.valueOf(vo.getCommentCount() == null ? 0 : vo.getCommentCount()), bodyFont, false);
                addCell(detail, String.valueOf(vo.getCollectCount() == null ? 0 : vo.getCollectCount()), bodyFont, false);
                String pub = vo.getPublishedAt() == null ? "" : df.format(vo.getPublishedAt());
                addCell(detail, pub, bodyFont, false);
            }
            doc.add(detail);
            doc.close();
        } catch (Exception ex) {
            log.error("导出 PDF 失败", ex);
            throw new RuntimeException("导出 PDF 失败", ex);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        if (header) {
            cell.setBackgroundColor(new java.awt.Color(220, 230, 241));
        }
        table.addCell(cell);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
