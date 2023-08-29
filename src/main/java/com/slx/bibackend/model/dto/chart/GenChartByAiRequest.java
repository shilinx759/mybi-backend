package com.slx.bibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 圖標生成請求
 *
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 名稱
     */
    private String name;
    /**
     * 分析目標
     */
    private String goal;
    /**
     * 圖標類型
     */
    private String chartType;


    private static final long serialVersionUID = 1L;
}