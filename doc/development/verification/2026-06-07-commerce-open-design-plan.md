# 2026-06-07 Commerce Open Design Plan

## Goal

Restore the commerce Open Design and Android Java/XML runtime screen so the
商城 page only shows real API-backed products and explainable capability
boundaries. Remove the fixed beginner-material supply activity banner and do
not replace it with any promotional or fake activity content.

## Scope

- In scope:
  - `doc/development/open-design/commerce-home-a.html`
  - `DouYu/app/src/main/res/layout/fragment_commerce_home.xml`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/CommerceFragment.java`
  - `DouYu/app/src/main/res/layout/activity_product_detail.xml`
  - `DouYu/app/src/main/res/layout/activity_payment_boundary.xml`
  - Offline unit tests under `DouYu/app/src/test`
- Out of scope:
  - Community, AI, messages, profile, settings, and other non-commerce pages.
  - Cart, order confirmation, real WeChat/Alipay SDK, real payment success
    states, or new operation/activity landing pages.
  - Real-device validation, adb install, instrumentation, screenshot pulling,
    and contact-sheet generation.

## Design Source

The Open Design daemon had no active project during planning, so this pass uses
the checked-in design source:

- `doc/development/open-design/commerce-home-a.html`

The Android runtime mapping remains:

- `commerce-home-a.html` -> `fragment_commerce_home.xml`
- Product card click -> `ProductDetailActivity` with `productId`
- Product detail payment boundary click -> `PaymentBoundaryActivity`

## Implementation Steps

1. Add offline regression tests before changing runtime or design copy.
2. Remove the fixed beginner-material supply banner and the visible activity instruction from the Open
   Design commerce page.
3. Replace the Android commerce fixed banner with a non-activity rule/boundary
   card that explains real API data and commerce limits.
4. Update `CommerceFragment` copy and product card subtitle formatting so cards
   show price, status, and type/category without fake stock, fake activity, or
   fake cart claims.
5. Keep product detail and payment boundary paths explicit and non-misleading.
6. Run offline static checks, unit tests, assemble, and lint.

## Offline Verification Commands

Run from the repository root unless a command changes directory:

```powershell
$removedCommerceTitle = '新手材料' + '补给'
$removedCommerceInstruction = '只展示真实可解释活动，' + '点击路径必须存在'
rg -n "$removedCommerceTitle|$removedCommerceInstruction" doc DouYu\app\src
rg --files DouYu/app/src | rg "\.kt$"
$forbiddenAndroidTerms = "compose|Composable|Navigation Compose|kotlinx|MockData|fake payment|假支付|假订单|$removedCommerceTitle"
rg -n $forbiddenAndroidTerms DouYu/app
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

## Device Validation

Real-device UI parity: not-covered in this worktree pass because user requested
no device validation.

Do not run:

- `adb install`
- `adb shell am instrument`
- screenshot pull
- contact-sheet generation

## Execution Log

- Plan document created before runtime implementation.
- Created offline regression coverage for:
  - Commerce Open Design/runtime XML no longer containing the removed activity
    title or instruction.
  - Commerce runtime XML retaining chips, list, loading, empty, and error
    state containers.
  - Product detail exposing a real payment-boundary click path.
  - Payment boundary CTA remaining disabled until real payment is connected.
  - Commerce product cards being created only when a backend product has a
    non-empty `productId` detail path.
- Updated `commerce-home-a.html` and `fragment_commerce_home.xml` so the fixed
  activity banner is now a non-promotional commerce rules/boundary card.
- Updated `CommerceFragment` so chips match the design categories and product
  card subtitles show price, status, and type/category without fake stock,
  fake activity, or fake cart claims.
- Used temporary process-local SDK variables for Gradle verification:
  `ANDROID_HOME=D:\AndroidChace` and `ANDROID_SDK_ROOT=D:\AndroidChace`.
- Offline verification completed:
  - Removed-commerce-copy static scan: pass.
  - Kotlin source scan under `DouYu/app/src`: pass, no `.kt` files.
  - Compose/Kotlin/mock runtime term scan under `DouYu/app/src`: pass.
  - `.\gradlew.bat :app:testDebugUnitTest --console=plain`: pass.
  - `.\gradlew.bat :app:assembleDebug --console=plain`: pass.
  - `.\gradlew.bat :app:lintDebug --console=plain`: pass.
- Real-device UI parity remains not-covered in this pass by user request. No
  `adb install`, instrumentation, screenshot pull, or contact-sheet generation
  was run.
