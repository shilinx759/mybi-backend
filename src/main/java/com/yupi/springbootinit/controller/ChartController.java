package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.BiMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表生成控制器
 *
 * @author slx
 * @from 2023.7.6
 */
@RestController
@RequestMapping("/chart")
@Slf4j
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;



//    private

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);


        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }



    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);


        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAi
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAi, HttpServletRequest request) {
        String name = genChartByAi.getName();
        String goal = genChartByAi.getGoal();
        String chartType = genChartByAi.getChartType();

        //校驗
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目標為空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名稱過長");

        //校驗文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //校驗文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超過 1M");

        //校驗文件後綴
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件後綴非法");

        User loginUser = userService.getLoginUser(request);

        //限流判斷，每個用戶一個限流器 方法名_用户id 作为key 作用：限制每个用户执行某个方法 XX 次
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());

        //excel 測試
        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        return ResultUtils.success(result);



        //定义模型id 即 BI模型
        long modelId = 1659171950288818178L;
        //构造用户输入
        /**
         * 分析需求:
         * 分析网站的用户增长趋势
         * 原始数据:
         * 日期,用户数
         * 1日,10
         * 2日,20
         * 3日,30
         */
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        //指定图表类型
        String userGoal = goal;
        //如果用户指定了图表类型，那么需要修改一下需求
        if (StringUtils.isNotBlank(chartType)) {
           userGoal += ",请使用" + chartType;
        }


        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData);
        //得到ai的回复长这这样：
        /**
         * 【【【【【
         * {
         *   "title": {
         *     "text": "用户增长趋势",
         *     "subtext": "数据来源: 网站统计",
         *     "left": "center"
         *   },
         *   "tooltip": {},
         *   "xAxis": {
         *     "type": "category",
         *     "data": ["1日", "2日", "3日"]
         *   },
         *   "yAxis": {
         *     "type": "value"
         *   },
         *   "series": [{
         *     "name": "用户数",
         *     "type": "line",
         *     "data": [10, 20, 30]
         *   }]
         * }
         * 【【【【【
         * 根据提供的原始数据，可以看出网站的用户增长趋势如下：
         * - 1日的用户数为10
         * - 2日的用户数为20
         * - 3日的用户数为30
         * 从数据图表中可以观察到，用户数在这三天内呈现逐渐增长的趋势，说明网站吸引了更多的用户。
         *
         * Process finished with exit code 0
         */
        String aiResult = aiManager.doChat(modelId, userInput.toString());
        //根据【【【分割符号来划分出我们要的代码段和分析文本
        String[] splits = aiResult.split("【【【【【");
        //生成成功的话会有三段
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        //分出代码段和分析文本 第0个是空字符串
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("succeed");


        //插入数据到数据库
        final boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        //设置返回类型，返回给前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());


        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过 1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFIleSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFIleSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        final val loginUser = userService.getLoginUser(request);
        //限流判断
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getUserRole());

        long biModeId = 1659171950288818178L;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");

        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");

        //压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        // 原始 wait 状态先插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //todo 建议处理任务队列满了后，抛异常的情况
        CompletableFuture.runAsync(()->{
            //先修改图表任务状态为“执行中，等执行成功后修改为“已完成”
            //保存执行结果，；执行失败后，修改为“失败”，记录任务失败信息
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");

            boolean b = chartService.updateById(updateChart);
            if (!b) {
                //没成功
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }
            //调用AI
            String result =aiManager.doChat(biModeId, userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            //todo 建议状态为枚举
            updateChartResult.setStatus("succeed");
            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
                return;
            }
        },threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步消息队列）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过 1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFIleSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFIleSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        final val loginUser = userService.getLoginUser(request);
        //限流判断
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getUserRole());

//        long biModeId = 1659171950288818178L;
        long biModeId = CommonConstant.BI_MODEL;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");

        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");

        //压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        // 原始 wait 状态先插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
//修改了表属性之后，在获取一次图表 ID
        long newChartId = chart.getId();
        //把这个图表传到生产者塞入队列，消费者再取出来拿去执行
        biMessageProducer.sentMessage(String.valueOf(newChartId));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败!" + chartId + "," + execMessage);
        }
    }


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {


        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();


        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
