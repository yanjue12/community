package com.fzg.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图表数据传输对象
 */
@Data
@NoArgsConstructor
public class ChartDataDTO {
    
    /**
     * 扇形图数据项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieItem {
        private String name;
        private Long value;
        private String color;   // 颜色（可选）
    }
    
    /**
     * 折线图/柱状图数据项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        private String date;
        private Long value;
        private Long draftValue;
        private String label;   // 标签（可选）
    }
    
    /**
     * 趋势数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private Long current;
        private Long previous;
        private Double changeRate;
        private String changeType;      // 变化类型：increase/decrease/stable
        private String period;          // 统计周期：day/week/month
    }
    
    /**
     * 热门排行数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankItem {
        private Long id;
        private String title;
        private Long value;
        private Integer rank;
        private String changeType;  // 排名变化：up/down/new/stable
    }
}