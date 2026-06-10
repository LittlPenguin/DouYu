# 2026-06-08 Commerce Real Products Repair

## Scope

This repair replaces the commerce home empty-list boundary with real API-backed
commerce content while preserving the Java/XML migration rules.

The dev database volume was not cleared. Product covers were selected from
reusable-license image sources, uploaded through the configured OSS flow, and
imported by an explicit dev-only command. The imported products are one-time dev
verification data, not runtime seed content.

## Runtime Record

| Check | Result |
| --- | --- |
| Branch | `main...origin/main [ahead 12]` |
| Dirty worktree | Present before this task; changes were preserved and built on. |
| Backend | `http://127.0.0.1:8081`, Spring Boot PID `27980`, health `UP`. |
| Docker | `douyu-postgres` and `douyu-redis` running and healthy. |
| Real device | `10.64.241.158:41817`, model `ELI_AN00`. |
| Volume policy | No `docker compose down -v`; no dev data deletion in this stage. |
| Backend log | `doyu-server/.qa-output/commerce-real-products-springboot.out.log` |

## Implementation Record

| Item | Status | Evidence |
| --- | --- | --- |
| Open Design commerce update | Closed | `doc/development/open-design/commerce-home-a.html` no longer contains the fixed rules card. |
| Commerce docs update | Closed | `doc/development/08-commerce-orders-address.md` records API-backed categories and masonry product cards. |
| Backend product dimensions | Closed | Flyway `V9__product_image_dimensions_category_name.sql`; `mvn test` passed. |
| Product category API | Closed | `GET /api/v1/product-categories` returns visible category counts. |
| Dev-only product import API | Closed | `PUT /api/v1/dev/commerce/products`, `@Profile("dev")`, login required. |
| Android commerce masonry UI | Closed | `CommerceFragment`, `CommerceProductAdapter`, `item_commerce_product.xml`; Android tests/build/lint passed. |
| Reusable image source list | Closed | `doc/development/verification/commerce-product-image-sources-2026-06-08.md`. |
| OSS product import | Closed with 10 products | `doyu-server/.qa-output/commerce-products-import-report.json`. |
| Real-device verification | Closed for commerce scope | Screenshot directory below. |

## API Evidence

Fresh API smoke was run after restarting the dev backend with Flyway v9 applied.

| Endpoint | Result |
| --- | --- |
| `/actuator/health` | `UP` |
| `/api/v1/products?page=1&size=30` | `items=10`, `total=10` |
| `/api/v1/product-categories` | `beads=3`, `boards=1`, `tools=2`, `kits=3`, `player=1` |
| `/api/v1/products?page=1&size=20&categoryId=beads` | 3 products: `commerce_real_color_beads`, `commerce_real_desk_beads`, `commerce_real_heart_edelweiss` |

All 10 imported products are `ON_SALE/PASS`, use OSS public URLs under
`https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/`, do not contain
`/seed/`, and include valid `imageWidth` / `imageHeight` values.

Two additional manifest candidates were not imported because the Commons or
download URL did not complete reliably. They were not replaced with
unclear-license media because the plan explicitly forbids using copyright-unclear
search images to fill the count. The imported count still satisfies the 10-15
product requirement.

## Verification Commands

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

Result: 69 tests, 0 failures, 0 errors.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

Result: all three commands exited `0`; `lintDebug` reported `BUILD SUCCESSFUL`.

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app
git diff --check
```

Result: `.kt` and Compose/Kotlin/mock scans had no output. `git diff --check`
had no whitespace errors; only line-ending warnings were printed.

Seed/demo scan note: the broad scan still matches historical verification docs
and explicit test fixture IDs such as `prod_beads_red`; no production runtime
seed implementation was identified in this stage.

## Real Device Evidence

APK install:

```powershell
adb -s 10.64.241.158:41817 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:41817 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
```

Result: both installs returned `Success`.

Instrumentation:

```powershell
adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.CommerceRealProductsInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

Results:

- `CommerceRealProductsInstrumentedTest`: `OK (1 test)`, time `39.882s`.
- `VisualSmokeInstrumentedTest`: `OK (1 test)`, time `126.784s`.

Screenshot evidence:

- Commerce dedicated screenshots:
  `doc/development/verification/android-java-xml-screenshots/2026-06-08-commerce-real-products/commerce-real-products/`
- Full visual smoke screenshots:
  `doc/development/verification/android-java-xml-screenshots/2026-06-08-commerce-real-products/visual-smoke/`
- Contact sheets:
  `commerce-real-products-contact-sheet.png`
  `visual-smoke-contact-sheet.png`

Commerce screenshots captured:

- `commerce_real_home.png`
- `commerce_category_1.png`
- `commerce_category_2.png`
- `commerce_real_product_detail.png`
- `visual-smoke/main_commerce.png`
- `commerce-running-final.png` proves the device was left on the commerce tab
  after verification.

Manual visual check from the pulled screenshots:

- The commerce page no longer shows `商城规则说明`.
- Real OSS product images render in the grid.
- The grid is two-column and product image heights vary with real image ratios.
- Top category chips switch between real categories and show corresponding products.
- Tapping a real product opens a real `ProductDetailActivity(productId)` screen with image, title, price, stock, category, status, audit status, and transaction boundary.
- No obvious 375dp-width overflow, text overlap, bottom-nav offset, raw HTTP error, fake success Toast, or empty click was visible in the captured commerce screenshots.

## Findings And Repairs

| ID | Severity | Area | Expected | Actual before repair | Status |
| --- | --- | --- | --- | --- | --- |
| CMR-001 | P1 | Commerce home copy | No fixed `商城规则说明` module. | Open Design still contained the old rules card. | Closed: Open Design and Android screenshots have no rules card. |
| CMR-002 | P1 | Commerce product data | Public list shows real `ON_SALE/PASS` products after explicit import. | Earlier public list did not contain the new real product set. | Closed: 10 OSS-backed products imported and API-smoked. |
| CMR-003 | P1 | Commerce layout | Product cards use real image ratio and are not uniform height. | Earlier commerce screen used the generic summary grid. | Closed: Android now uses `StaggeredGridLayoutManager` and `CommerceProductAdapter`; screenshots show varied heights. |
| CMR-004 | P2 | Source coverage | Import 10-15 reusable-license product covers. | Two extra candidates failed download; replacing with unclear-license images would violate the plan. | Closed with note: 10 images imported, satisfying the lower bound. |

## Runtime Left Running

Per the user request, the environment was left running after verification:

- Spring Boot remains listening on `8081`, PID `27980`.
- Docker Compose services `douyu-postgres` and `douyu-redis` remain running.
- The debug APK remains installed on `10.64.241.158:41817`.
