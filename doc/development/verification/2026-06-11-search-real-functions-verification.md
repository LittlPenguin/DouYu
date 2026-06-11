# 2026-06-11 Search Real Functions Verification

## Scope

- Android `SearchActivity` is converted from a UI-only placeholder into a real Java + XML search surface.
- Backend adds `GET /api/v1/search` for retained public searchable surfaces:
  - posts
  - products
  - users
  - topics
- Android calls the unified backend search endpoint through Retrofit and `DoyuRepository`.

## Verified Behavior

- Empty keyword returns an empty page and Android renders input guidance instead of fake local results.
- `type=all` aggregates public retained surfaces.
- `type=posts`, `products`, `users`, and `topics` filter the backend result type.
- Backend excludes products that are not `ON_SALE` + `PASS`.
- Android result actions:
  - post results open `PostDetailActivity`.
  - product results open `ProductDetailActivity`.
  - topic results switch the search page to the posts filter and search by topic title.
  - user results show the public result data returned by the backend without fake profile navigation.
- Search page controls are executable:
  - search input
  - clear
  - cancel
  - retry
  - all five scope tabs

## Commands Run

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn -Dtest=DouyuBackendContractTests#globalSearchReturnsRealRetainedResultsAndHonorsFilters test
```

Result: passed. The test first failed red with `/api/v1/search` returning 401 before implementation, then passed after adding `SearchController` and the public GET whitelist.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.SearchContractTest" --tests "cn.edu.app.douyu.network.DoyuRepositorySearchTest" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest.searchApiUsesBackendSearchEndpoint" --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean
rg -n "UI-only|待接入|开发态|等待搜索接口|本地假结果|fake|mock" DouYu\app\src\main\java\cn\edu\app\douyu\feature\community\SearchActivity.java DouYu\app\src\main\java\cn\edu\app\douyu\feature\community\SearchResultAdapter.java DouYu\app\src\main\res\layout\activity_search.xml DouYu\app\src\main\res\layout\item_search_result.xml doyu-server\src\main\java\cn\edu\app\douyu\server\search\SearchController.java
```

Result: no matches.

```powershell
cd D:\Studio\SpellBean
rg --files DouYu\app\src | rg "\.kt$"
```

Result: no output.

```powershell
cd D:\Studio\SpellBean
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu\app -S
```

Result: no output.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:lintDebug --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

Result: passed, 64 tests.

```powershell
cd D:\Studio\SpellBean
git diff --check
```

Result: no whitespace errors. Git reported line-ending warnings for existing touched files.

## Not Covered

- No emulator or physical-device visual smoke was run in this pass.
- No screenshot parity capture was produced for `SearchActivity`.
- No long-running backend or Android service was left running after verification.
