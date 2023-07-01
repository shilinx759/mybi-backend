package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Chart;

/**
* @author 86181
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-06-30 13:48:35
*/
public interface ChartService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
}
