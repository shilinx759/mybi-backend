package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * BI 的返回结果
 * @author slx
 * @time 18:10
 */
@Data //自动生成getter setter
public class BiResponse {
    //加入数据库中定义的返回字段
    private String genChart;

    private String genResult;

    private long chartId;
}
