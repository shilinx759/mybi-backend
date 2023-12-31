package com.slx.bibackend.model.dto.chart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑请求
 *
* @author slx
 */
@Data
public class ChartEditRequest implements Serializable {
    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 圖標名稱
     */
    private String name;

    /**
     * 图表类型
     */
    private String chartType;



    private static final long serialVersionUID = 1L;
}