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
