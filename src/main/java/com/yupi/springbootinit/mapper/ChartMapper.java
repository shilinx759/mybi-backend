package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Map;

/**
* @author 86181
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-06-30 13:48:35
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {
    List<Map<String, Object>> queryChartData(String querySql);
}




