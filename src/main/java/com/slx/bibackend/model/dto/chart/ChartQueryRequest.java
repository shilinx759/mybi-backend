package com.slx.bibackend.model.dto.chart;

import com.slx.bibackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
* @author slx
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    private Long id;

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

    /**
     * 用户id
     */
    private Long userId;



    private static final long serialVersionUID = 1L;
}