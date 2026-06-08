# 商城真机校验与差异修复记录

Date: 2026-06-07
Branch: `codex/commerce-open-design-boundary`
Scope: 商城首页、商品详情、支付边界。其他主 Tab 截图只作为 smoke harness 副产物保存，不做本次差异修复范围。

## 依据

- Open Design source: `doc/development/open-design/commerce-home-a.html`
- Android layouts:
  - `DouYu/app/src/main/res/layout/fragment_commerce_home.xml`
  - `DouYu/app/src/main/res/layout/activity_product_detail.xml`
  - `DouYu/app/src/main/res/layout/activity_payment_boundary.xml`
- Android code:
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/CommerceFragment.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/PaymentBoundaryActivity.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/ui/XmlPageActivity.java`
- 文档边界: 商城列表只展示后端真实商品；没有真实商品时显示空态；商品详情必须通过真实 `productId` 请求；支付页只能展示联调/UI-only 边界，不能展示正式支付成功态。

## 环境记录

Device serial used: `10.64.241.158:39521`

| Item | Result |
| --- | --- |
| Model | `ELI-AN00` |
| Android version | `16` |
| Screen size | `1200x2664` |
| Density | `520` |
| Host WLAN IP | `10.64.241.153` |
| Backend candidate | `http://10.64.241.153:8081/` |
| Host health | `http://127.0.0.1:8081/actuator/health` returned `{"groups":["liveness","readiness"],"status":"UP"}` |
| Device health | `http://10.64.241.153:8081/actuator/health` returned `{"groups":["liveness","readiness"],"status":"UP"}` |
| Device root reachability | `http://10.64.241.153:8081/` returned HTTP `401`, proving the host is reachable and guarded |
| Backend profile | Current process command line contains `spring-boot:run -Dspring-boot.run.profiles=dev` and `--spring.profiles.active=dev` |

`RealBackendSmokeInstrumentedTest` was not run because the backend was confirmed as `dev`, not `qa-empty`. No backend data was reset or modified.

## Build And Test Commands

Environment used for Android commands:

```powershell
$env:ANDROID_HOME='D:\AndroidChace'
$env:ANDROID_SDK_ROOT='D:\AndroidChace'
$env:DOUYU_ANDROID_API_BASE_URL='http://10.64.241.153:8081/'
$env:DOUYU_ANDROID_CLEARTEXT_HOSTS='10.64.241.153,10.0.2.2,localhost'
```

Offline verification:

| Command | Result |
| --- | --- |
| Removed commerce activity copy scan | No matches |
| `rg --files DouYu/app/src | rg '\.kt$'` | No matches |
| Compose/mock/payment-forbidden source scan under `DouYu/app/src` | No matches |
| `.\gradlew.bat :app:testDebugUnitTest --console=plain` | `BUILD SUCCESSFUL` |
| `.\gradlew.bat :app:assembleDebug --console=plain` | `BUILD SUCCESSFUL` |
| `.\gradlew.bat :app:lintDebug --console=plain` | `BUILD SUCCESSFUL` |
| `.\gradlew.bat :app:assembleDebugAndroidTest --console=plain` | `BUILD SUCCESSFUL` |

Real-device verification:

| Command | Result |
| --- | --- |
| `adb -s 10.64.241.158:39521 install -r app\build\outputs\apk\debug\app-debug.apk` | `Success` |
| `adb -s 10.64.241.158:39521 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk` | `Success` |
| `adb -s 10.64.241.158:39521 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner` | `OK (1 test)`, time `128.094` seconds |

## Screenshot Evidence

Pulled from device directory:

`/storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke`

Saved to:

`doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke`

Contact sheet:

`doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke-contact-sheet.png`

Screenshot count: `22`
Dimensions: every PNG is `1200x2664`
Contact sheet size: `960x3450`

Commerce screenshots inspected:

| Screenshot | Status |
| --- | --- |
| `main_commerce.png` | Covered. Shows 商城 title, category chips, `真实商品边界` card, real-data empty state, and bottom nav. Removed activity banner is not visible. |
| `product_detail.png` | Covered. Fake smoke product id resolves to business boundary `内容不存在或已下架。`; raw HTTP detail is not shown. Payment boundary button path is visible. |
| `payment_boundary.png` | Covered. Shows 支付边界 title, service-side status text, no formal payment success state, and readable disabled UI-only CTA. |

## Findings And Fixes

| ID | Finding | Evidence | Fix | Status |
| --- | --- | --- | --- | --- |
| CDV-001 | Product detail smoke opened a non-existing product id and surfaced a raw technical `HTTP 404` message instead of the documented missing/down boundary. | First visual-smoke pass on `product_detail.png`. | Added `XmlPageActivityTest` coverage and mapped `HTTP 404` in `XmlPageActivity.userFacingDetailError()` to `内容不存在或已下架。` | Closed. Final `product_detail.png` shows the business boundary. |
| CDV-002 | Disabled payment boundary CTA was present but too low contrast on device, making the UI-only boundary hard to read. | First visual-smoke pass on `payment_boundary.png`. | Added node-level XML regression in `OpenDesignLayoutMappingTest` requiring disabled state plus explicit text color, surface background, stroke color, and stroke width. Updated `activity_payment_boundary.xml` while keeping `android:enabled="false"`. | Closed. Final `payment_boundary.png` shows readable disabled CTA text. |
| CDV-003 | Real-backend detail smoke could not be responsibly run against the current service because backend profile is `dev`, not `qa-empty`. | Process command line and health checks. | No app code change. Recorded as environment not-covered and did not modify backend data. | Remaining / not-covered by environment. |

## Final Commerce Assessment

- 商城首页 follows the Open Design commerce structure within the real-data boundary: category chips, explanatory commerce rules card, empty state when backend returns no items, and no local fake products.
- Product card navigation remains limited to backend products with a non-blank `productId`; smoke detail path for a non-existing id now displays a clear missing/down boundary.
- Product detail to payment boundary route exists through `product_payment_boundary`.
- Payment boundary stays UI-only/disabled and does not claim WeChat/Alipay production availability or a successful payment result.
- `RealBackendSmokeInstrumentedTest` remains not-covered in this pass because the available backend profile is `dev`.
