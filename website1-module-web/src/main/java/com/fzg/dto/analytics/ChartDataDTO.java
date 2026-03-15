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
        private String name;    // 名称
        private Long value;     // 数值
        private String color;   // 颜色（可选）
    }
    
    /**
     * 折线图/柱状图数据项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        private String date;    // 日期
        private Long value;     // 数值
        private String label;   // 标签（可选）
    }
    
    /**
     * 趋势数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private Long current;           // 当前值
        private Long previous;          // 上期值
        private Double changeRate;      // 变化率（百分比）
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
        private Long id;            // ID
        private String title;       // 标题
        private Long value;         // 数值
        private Integer rank;       // 排名
        private String changeType;  // 排名变化：up/down/new/stable
    }
}