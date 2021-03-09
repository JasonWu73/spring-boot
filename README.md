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

> 设置两个类别的 Token 好处在于:
> 1. 访问 Token 可拷贝给其他系统或用户使用, 而不用传递用户名密码
> 2. 杜绝了以同一 Token 刷新 Token, 从而获取对系统的无期限访问权问题

![Access Token 管理流程图](https://raw.githubusercontent.com/JasonWu73/spring-boot/master/docs/images/Access%20Token%20%E7%AE%A1%E7%90%86%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

### 前端 Token 获取及续期代码 (JavaScript)

自定义 Axios 实例及请求拦截器 (此代码极其能用, 稍微改改就可应用于实际项目中):

```javascript
import axios from 'axios';

const URL_GET_TOKEN = '/auth/access_token';
const URL_REFRESH_TOKEN = '/auth/access_token/refresh';

const LOCAL_KEY_EXPIRE = 'expire';
const LOCAL_KEY_TOKEN = 'token';
const LOCAL_KEY_REFRESH_TOKEN = 'refresh_token';

const httpClient = axios.create({
  baseURL: 'http://api.wxj.com/'
});

const getExpire = () => localStorage.getItem(LOCAL_KEY_EXPIRE);

const getToken = () => localStorage.getItem(LOCAL_KEY_TOKEN);

const getRefreshToken = () => localStorage.getItem(LOCAL_KEY_REFRESH_TOKEN);

const isNeedRefreshToken = () => Date.now() >= getExpire();

const saveToken = ({expire, token, refreshToken}) => {
  localStorage.setItem(LOCAL_KEY_EXPIRE, Date.now() + expire * 1000 + ''); // 时间戳以毫秒为单位, 过期以秒为单位
  localStorage.setItem(LOCAL_KEY_TOKEN, token);
  localStorage.setItem(LOCAL_KEY_REFRESH_TOKEN, refreshToken);
};

httpClient.interceptors.request.use(async config => {
  if (config.url.includes(URL_GET_TOKEN) || config.url.includes(URL_REFRESH_TOKEN)) {
    return config;
  }

  if (isNeedRefreshToken()) {
    const params = new URLSearchParams();
    params.append('token', getRefreshToken());

    try {
      const response = await httpClient.post(URL_REFRESH_TOKEN, params);
      saveToken(response.data.data);
    } catch (err) {
      console.error(`Token 续期失败: ${err?.response?.data?.error}`);
      throw new Error(`${err?.response?.data?.error}, 请重新登录`);
    }
  }

  getToken() && (config.headers['Authorization'] = `Bearer ${getToken()}`);

  return config;
}, err => {
  return Promise.reject(err);
});

export {URL_GET_TOKEN, httpClient, saveToken};
```

登录代码 (以 Vue 2 为例):

```vue
<template>
  <div>
    <el-row>
      <el-col :span="8" :offset="8">
        <el-input v-model="username" placeholder="请输入用户名"></el-input>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8" :offset="8">
        <el-input v-model="password" placeholder="请输入密码"></el-input>
      </el-col>
    </el-row>
    <div>
      <el-button type="primary" @click="handleLoginClick" style="width: 250px;">登录</el-button>
    </div>
  </div>
</template>

<script>
  import {httpClient, saveToken, URL_GET_TOKEN} from '@/common/axios';

  export default {
    name: "Login",
    data() {
      return {
        username: '',
        password: ''
      };
    },
    methods: {
      async handleLoginClick() {
        try {
          const params = new URLSearchParams();
          params.append('username', this.username);
          params.append('password', this.password);

          const response = await httpClient.post(URL_GET_TOKEN, params);
          saveToken(response.data.data)

          alert('登录成功');
        } catch (err) {
          alert(`${err?.response?.data?.error || err.message} 💥`);
        }
      }
    }
  };
</script>

<style scoped>
  .el-row {
    margin-bottom: 15px;
  }
</style>
```

新闻页, 即需要登录后才可获取新闻列表 (以 Vue 2 为例):

```vue
<template>
  <el-table
    :data="bookList"
    style="width: 100%">
    <el-table-column
      prop="addTime"
      label="日期"
      width="180">
    </el-table-column>
    <el-table-column
      prop="title"
      label="标题"
      width="180">
    </el-table-column>
    <el-table-column
      prop="type"
      label="类型">
    </el-table-column>
  </el-table>
</template>

<script>
  import {httpClient} from '@/common/axios';

  export default {
    name: "News",
    data() {
      return {
        bookList: []
      };
    },
    async created() {
      await this.getBooks();
    },
    methods: {
      async getBooks() {
        try {
          const response = await httpClient.get('/es/news/all');
          const {data} = response.data;
          this.bookList = data;
        } catch (err) {
          alert(`${err?.response?.data?.error || err.message} 💥`);
        }
      }
    }
  };
</script>

<style scoped>

</style>
```
