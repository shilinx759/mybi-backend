package com.slx.bibackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slx.bibackend.model.entity.Chart;
import com.slx.bibackend.service.ChartService;
import com.slx.bibackend.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author 86181
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-06-30 13:48:35
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




