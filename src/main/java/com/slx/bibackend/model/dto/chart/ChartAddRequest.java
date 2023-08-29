package com.slx.bibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
* @author slx
 */
@Data
public class ChartAddRequest implements Serializable {


    /**
     * 分析目标
     */
    private String goal;


    /**
     * 圖標名稱
     */
    private String name;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;



    private static final long serialVersionUID = 1L;
}