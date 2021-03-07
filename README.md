# spring-boot

Spring 框架学习项目

> 项目思想:
> 1. 各个 web 服务应该假设为内网服务, 接口的访问应该由网关进行路由
> 2. 各服务的身份验证由网关统一处理
     >    1. 网关将 JWT (JSON Web Token) 签发路由给身份验证服务
>    2. 网关通过 REST API 调用身份验证服务完成 JWT 验签功能
> 3. 各服务可在请求头中得到由网关写入的身份验证标识符 (本项目以用户名为唯一标识)
> 4. 目前网关实现
     >    1. 反向代理
>    2. 熔断机制 (服务降级)

为了模拟网关的真实性, 可以在本机设定一个假域名, 即修改 hosts 文件 (以 MacOS 为例):

`$ vim /private/etc/hosts`:

```
# 开发测试目的
127.0.0.1 api.wxj.com
```

## 项目目录结构

```
.
├── auth  // 身份验证模块 (web 项目)
├── common // 公共模块 (jar 项目)
├── docs // 非 Maven 模块 (存放一些文档资源等)
├── elasticsearch // Elasticsearch (web 项目)
├── gateway // Spring Cloud Gateway (web 网关)
├── redis // Redis (web 项目)
└── web // 公共 Spring Boot web 配置模块 (jar 项目)
```

## Token 续期策略

1) 主动续期

由后端在 Token 验证通过且达到指定阈值时, 自动续期 (类似服务端 session 有效期策略)

> 适用场景: 前后端分离的后台, 安全性要求不高的服务

2) 被动刷新 (推荐)

由客户端请求 Token 刷新接口完成续期

1. 生成 Token 分为两类: (1) 用户于访问; (2) 用于刷新 (可避免输入用户名密码)
2. 用于刷新的 Token 有效期是用于访问的两倍
3. 刷新只有在访问 Token 过期后才能成功

![Token 验证流程图](https://raw.githubusercontent.com/JasonWu73/spring-boot/master/docs/images/Token%20%E9%AA%8C%E8%AF%81.png)
