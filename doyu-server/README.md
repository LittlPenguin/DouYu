# 豆屿 Doyu 后端服务

第一阶段后端是 Spring Boot 模块化单体，面向 Android 联调提供 `/api/v1` REST API。真实短信、OSS、AI、微信支付和支付宝支付暂以服务端 Stub Provider 实现，不提交任何密钥。

## 本地启动

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

接口文档：

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- 健康检查: `http://localhost:8080/actuator/health`

## 测试

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 开发 Stub

- 短信验证码：`123456`
- 测试管理员：`admin / admin123`，仅 `dev/test` 配置使用。
- 上传预签名：返回本地 Stub URL，不暴露 OSS Secret。
- AI 图纸任务：同步 Stub 流转为 `SUCCEEDED`，并生成可联调的图纸和材料清单。
- 支付：创建微信/支付宝 App 支付 Stub 参数，回调接口按支付单和渠道交易号幂等处理。
