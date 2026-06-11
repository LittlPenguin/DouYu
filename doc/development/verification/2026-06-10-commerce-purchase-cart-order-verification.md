# 2026-06-10 商品购买、购物车与下单验证

## 范围

- 商品详情页数量选择、加入购物车、立即创建订单和手动地址快照输入。
- 购物车页面加载、行选择、数量更新、删除和购物车下单创建订单。
- 后端购物车/订单契约、库存锁定、幂等、购物车下单清理、取消订单释放库存和支付运行时移除。
- 商品详情、购物车、数量、收货信息和订单创建相关 UI 文案必须使用自然中文。

## 命令与结果

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.commercePurchaseCartAndOrderUiUsesRealBackendContracts" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest" --tests "cn.edu.app.douyu.network.DoyuRepositoryCommercePurchaseTest" --tests "cn.edu.app.douyu.feature.commerce.PurchaseFormStateTest" --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=CommercePurchaseContractTests test`
  - 结果：通过，`Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`。

- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests#orderCreationIsIdempotentAndGuardsInventoryAndCancelState test`
  - 结果：通过，`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。
  - 备注：该命令在旧库存保护测试对齐新契约后重跑。新契约中加入购物车会拒绝超库存数量，立即下单仍校验下单时库存保护。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`，`ANDROID_UNIT_TESTS_EXIT_0`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:lintDebug --console=plain`
  - 初始结果：因既有 `CAMERA` 权限触发 `PermissionImpliesUnsupportedChromeOsHardware` 失败。
  - 已应用修复：在 `AndroidManifest.xml` 增加 `<uses-feature android:name="android.hardware.camera" android:required="false" />`。
  - 重跑结果：通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\doyu-server; mvn test`
  - 结果：通过，`Tests run: 62, Failures: 0, Errors: 0, Skipped: 0`。

- `rg -n "payment|payments|refund|refunds|Payment|Refund|callback|wechat|alipay" doyu-server/src/main/java DouYu/app/src/main/java DouYu/app/src/main/res -S`
  - 结果：`NO_PAYMENT_RUNTIME_MATCHES`。

- `rg --files DouYu/app/src | rg "\.kt$"`
  - 结果：`NO_ANDROID_KT_FILES`。

- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData" DouYu/app/src -S`
  - 结果：`NO_COMPOSE_KOTLINX_OR_MOCKDATA_MATCHES`。

- `rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main -S`
  - 结果：`NO_BACKEND_RUNTIME_SEED_MATCHES`。

## 覆盖说明

- 后端契约覆盖：已由自动化测试覆盖。
- Android 网络/model/源码契约覆盖：已由 Android 单元测试和源码检查覆盖。
- Android 构建和 lint 覆盖：已由上方 Gradle 命令覆盖。
- 支付运行时移除：已由生产源码/资源搜索和移除功能测试覆盖。
- 真实设备购买/购物车 smoke：本轮未覆盖，未捕获 Android 设备或模拟器截图。
- 商品购买/购物车 Open Design 视觉一致性：本轮未用截图覆盖。文本搜索 `doc/development/open-design` 未发现商品详情、购物车、结算或加入购物车的权威页面，除既有商城首页外无对应页面，因此本轮依赖源码/布局契约以及 build/lint 覆盖。

## 中文化复验

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.commercePurchaseCartAndOrderUiUsesRealBackendContracts" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.commercePurchaseCartAndOrderUiCopyIsChinese" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest" --tests "cn.edu.app.douyu.network.DoyuRepositoryCommercePurchaseTest" --tests "cn.edu.app.douyu.feature.commerce.PurchaseFormStateTest" --console=plain`
  - 红灯：新增 `commercePurchaseCartAndOrderUiCopyIsChinese` 后，当前英文文案断言失败，失败位置为 `OpenDesignLayoutMappingTest.java:295`。
  - 绿灯：完成商品详情、购物车、收货信息、数量和订单创建文案中文化后重跑通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- UTF-8 源码/文档扫描：
  - 结果：目标 Java/XML 和购买链路相关文档没有命中本轮检查的英文 UI 文案，也没有命中乱码片段。

## 购物车库存上限修复复验

- 修复范围：Android 商品详情页加入购物车前读取当前购物车，把同一 SKU 已有数量计入库存上限；购物车页加号达到库存上限时前端拦截；购买/购物车链路把 `HTTP 409`、`Inventory is not enough`、`CONFLICT` 以及 Activity 通用错误包装后的冲突文案转换为中文业务提示。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.feature.commerce.PurchaseFormStateTest.commerceConflictErrorsMapToChineseInventoryMessage" --console=plain`
  - 红灯：新增 `CONFLICT` 兜底断言后失败，当前会原样返回 `CONFLICT`。
  - 第二次红灯：补充 `请求失败：CONFLICT。请返回后重试。` 断言后失败，Activity 通用错误包装后的冲突文案仍会透出。
  - 绿灯：补充 `CONFLICT` 包含型中文业务提示后重跑通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.feature.commerce.PurchaseFormStateTest" --tests "cn.edu.app.douyu.network.DoyuRepositoryCommercePurchaseTest" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest" --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
  - 结果：通过，`BUILD SUCCESSFUL`。

- 源码约束扫描：
  - `rg --files DouYu/app/src | rg "\.kt$"`：`NO_ANDROID_KT_FILES`。
  - `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|mock data|demo list|fake order" DouYu/app/src -S`：`NO_COMPOSE_KOTLINX_OR_MOCKDATA_MATCHES`。
  - `rg -n "HTTP 409|Inventory is not enough|Insufficient stock" DouYu/app/src/main/java -S`：`NO_PRODUCTION_409_OR_ENGLISH_INVENTORY_MATCHES`。

- Open Design 记录：本轮未修改布局结构，只调整购买/购物车数量行为与状态文案。沿用上方既有结论：`doc/development/open-design` 未提供商品详情、购物车、结算或加入购物车的权威页面；本轮未做真机或截图视觉覆盖。
