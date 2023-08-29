# 智能 BI 项目

## 项目简介
基于 Spring Boot+Rabbit MQ+Ant Design Pro+AIGC 的智能数据分析平台。区别于传统 BI 系统用户只需导入原始数据（如Excel文件）便可得到由AI生成的分析图标及文字结论。

### 主流框架 & 特性

##### 后端：
- Spring Boot 2.7.x（贼新）
- Spring MVC
- MyBatis + MyBatis Plus 数据访问（开启分页）
- Spring Boot 调试工具和项目处理器
- Redis+Redisson 限流
- Rabbit MQ 消息队列
- AI SDK
- Easy Excel 表格处理
- Swagger + Knife4j 接口文档生成
- Hutool+Apache Common Utils 等工具库

##### 前端：

- Ant Design Pro 5.x开发脚手架
- Echarts 图表生成库
- OpenAPI 前端代码生成

### 数据存储

- MySQL 数据库
- Redis 内存数据库

## 业务流程

首先获取用户输入及分析原始，对用户输入进行解析和压缩，再使用根据预设好的 Prompt 模板对用户输入进行封装，
通过调用 AIGC 接口生成可视化图表及分析结论，返回给前端渲染。

### 架构设计

![BI 系统架构.drawio.png](https://raw.githubusercontent.com/shilinx759/mybi-backend/master/doc/BI%20%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84.drawio.png)

### 业务功能

1. 由于 AIGC 的输入 Token 限制，使用Easy Excel解析用户上传的XLSX表格数据文件并压缩为CSV，实测提高了单次输入数据量、并节约了成本。
2. 为保证系统的安全性，对用户上传的原始数据文件进行了后缀名、大小、内容等多重校验(其实能被Easy Excel解析成功，内容基本是没问题的)
3. 为防止某用户恶意占用系统资源，基于 Redisson 的 RateLimiter 实现分布式限流，控制单用户访问的频率
4. 由于 AIGC 的响应时间较长，基于自定义 IO 密集型线程池+任务队列实现了AIGC 的并发执行和异步化，提交任务后即可响应前端，支持更多用户排队而不是无限给系统压力导致提交失败，提高用户体验。
5. 由于本地任务队列重启丢失数据，使用 RabbitMQ(分布式消息队列)来接受并持久化任务消息，通过 Direct 交换机转发给解耦的 AI 生成模块消费并处理任务，提高了系统的可靠性



### MySQL 数据库建表

#### 图表表
- id
- 分析目标
- 图表原始数据
- 图表类型
- AI 生成图表数据
- AI 生成分析结论
- 创建者 ID
- 创建时间
- 更新时间
- 逻辑删除

#### 用户表
- 账号
- 密码
- 用户昵称
- 用户角色 （user，Admin）
- 创建时间
- 更新时间
- 逻辑删除

