# 2026-06-07 Community Real Content Repair

## Scope

This record covers the community-home repair requested after the Java/XML
rewrite:

- Rebuilt the default dev PostgreSQL and Redis volumes.
- Imported one-time real community posts with reusable bead-art images uploaded
  through the configured Aliyun OSS provider.
- Replaced fixed-height community cards with API-backed topic chips and
  ratio-based two-column masonry cards.
- Removed the old community explanatory copy `瀑布流内容发现`.
- Re-ran backend, Android, API, and real-device verification.
- Left the dev backend, PostgreSQL, Redis, and Android app running for manual
  review.

## Data Policy

The default dev volume was intentionally rebuilt in this stage. This removed
historical local dev data, including old `post_seed%`, `prod_%`, topics,
stickers, tokens, orders, uploads, AI jobs, and other dev rows. The `qa-empty`
containers were not removed.

The imported community posts are development content created by an explicit
one-time script:

- Script: `doyu-server/scripts/import-community-real-content.ps1`
- Manifest: `doyu-server/scripts/community-real-content-manifest.json`
- Report: `doyu-server/community-real-content-import-report.json`

They are not runtime seeders and do not run during Spring Boot startup. A clean
rebuilt database still starts empty until the import script is executed.

## Implementation Summary

Backend:

- Added post cover dimensions migration `V8__post_cover_dimensions.sql`.
- Returned `coverImageUrl`, `coverWidth`, `coverHeight`, `topicIds`, and
  `topicNames` in community post views.
- Made approved admin audit move posts into the public `VISIBLE` status.
- Added a dev-profile-only topic import endpoint for explicit one-time imports.

Android:

- Added `Topic` model and API/repository calls for `/api/v1/topics` and
  `/api/v1/topics/{topicId}/posts`.
- Rebuilt `CommunityFragment` to use real topic chips and a
  `StaggeredGridLayoutManager`.
- Added `CommunityPostAdapter` and `item_community_post.xml`; image height is
  calculated from cover width/height and clamped to 120-260dp.
- Removed the old explanatory community card and the `瀑布流内容发现` text from
  app UI.
- Added stale-token handling for public community reads: if a rebuilt dev volume
  invalidates an old local token, community feed/topic/detail reads clear the
  stale token and retry anonymously.
- Updated `PostDetailActivity` to load the real post cover image with Glide.

## Import Evidence

Command:

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose down -v
docker compose up -d postgres redis
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\import-community-real-content.ps1 -BaseUrl 'http://127.0.0.1:8081' -OutputPath '.\community-real-content-import-report.json'
```

Clean-before-import evidence:

- `GET /api/v1/posts/feed?page=1&size=20` returned `total=0`.
- Database counts before import: `posts=0`, `topics=0`, `file_assets=0`,
  `admin_users=1`.

Imported image/content records:

| Title | Topic | Size | License | Author | Source |
| --- | --- | ---: | --- | --- | --- |
| 爱心挂件的边缘留白 | 新手教程 | 2448x3264 | CC BY-SA 4.0 | Saintfevrier | <https://commons.wikimedia.org/wiki/File:Perler_bead_heart_beaded_side.jpg> |
| 熨烫后的背面质感 | 教程步骤 | 2448x3264 | CC BY-SA 4.0 | Saintfevrier | <https://commons.wikimedia.org/wiki/File:Perler_bead_heart_fused_side.jpg> |
| 小图标适合试配色 | 配色灵感 | 401x341 | CC BY-SA 4.0 | IloveGreece | <https://commons.wikimedia.org/wiki/File:Perler_bead_heart_fused_side_cropped_background.png> |
| 低饱和色卡更耐看 | 配色灵感 | 1880x1316 | CC BY-SA 3.0 | Superbass | <https://commons.wikimedia.org/wiki/File:Buegelperlen_1.jpg> |
| 把常用色放在右手边 | 工作台 | 1781x1188 | CC BY-SA 4.0 | Superbass | <https://commons.wikimedia.org/wiki/File:2022-01-06-B%C3%BCgelperlen-6920.jpg> |
| 按色系分盒最省时间 | 工作台 | 1280x954 | CC BY-SA 2.0 | *Sally M* | <https://commons.wikimedia.org/wiki/File:Buegelperlen_perler_beads.jpg> |
| 完成后先平放冷却 | 作品展示 | 2232x2232 | CC BY-SA 2.0 | Bjorn Rixman | <https://commons.wikimedia.org/wiki/File:Parlplatta_(8457346).jpg> |
| 长图需要控制首屏高度 | 作品展示 | 5376x3024 | CC BY-SA 4.0 | Oddjob | <https://commons.wikimedia.org/wiki/File:StockholmArlandaPlasticBeadsMosaic.jpg> |
| 镊子尖端决定速度 | 工具收纳 | 2048x1536 | CC BY 2.5 | Aney | <https://commons.wikimedia.org/wiki/File:Plastic_beads1.jpg> |
| 新手先拼轮廓再填色 | 新手教程 | 1024x768 | Public domain | Pseudopanax | <https://commons.wikimedia.org/wiki/File:Hama_beads.jpg> |

All imported `ossUrl` values use the Aliyun OSS public base URL configured in
the local environment. No secret values were copied into this record.

## API Evidence

Fresh checks after import:

```json
{
  "health": { "status": "UP", "groups": ["liveness", "readiness"] },
  "feed": { "code": "OK", "total": 10, "items": 10 },
  "topics": { "code": "OK", "total": 6, "items": 6 },
  "topic_beginner": { "code": "OK", "total": 2, "items": 2 }
}
```

Database validation after import:

```text
file_assets        10
posts              10
visible_posts      10
topics              6
local_urls          0
seed_urls           0
missing_dimensions  0
```

UTF-8 response validation:

- Python decoded the feed response as UTF-8 and verified Chinese title,
  content, author, and topic strings.
- Mojibake candidate count: `0`.

## Test Evidence

Static checks:

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
```

Results:

- Android `.kt` check: no output.
- Android Compose/Kotlin/mock check: no output.
- Backend seed check: only test assertions mention `DataInitializer`; no
  production runtime seed/demo content was found.

Backend:

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

Result: `Tests run: 63, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

Android:

```powershell
cd D:\Studio\SpellBean\DouYu
$env:DOUYU_ANDROID_API_BASE_URL='http://10.64.241.153:8081/'
$env:DOUYU_ANDROID_CLEARTEXT_HOSTS='10.64.241.153,10.0.2.2,localhost,127.0.0.1'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --console=plain
```

Result: `BUILD SUCCESSFUL`. Gradle output also reported
`compileDebugKotlin NO-SOURCE` and `compileDebugAndroidTestKotlin NO-SOURCE`.

## Real Device Evidence

Device:

- Serial used for final launch: `10.64.241.158:38673`
- Model: `ELI-AN00`
- Android: `16`
- Physical size: `1200x2664`
- Density: `520`

Evidence directory:

`doc/development/verification/android-java-xml-screenshots/community-real-content/`

Key screenshots:

- `manual/community_final_home.png`
  - Community home displays real OSS bead images.
  - Two-column masonry heights are non-uniform and derived from cover ratios.
  - Real topic chips are visible.
  - No `瀑布流内容发现` copy.
  - No login boundary after stale-token retry.
- `manual/community_final_topic_beginner.png`
  - `新手教程` chip is selected.
  - The page shows the two real posts returned by
    `/api/v1/topics/topic_beginner/posts`.
- `manual/community_final_post_detail.png`
  - Tapping a real card opens `PostDetailActivity`.
  - Detail page shows real `postId`, title, author, content, status, and cover
    image.

Contact sheet:

`doc/development/verification/android-java-xml-screenshots/community-real-content/community-real-content-contact-sheet.png`

## Final Running State

The project was intentionally left running for manual verification.

- Backend URL: `http://127.0.0.1:8081`
- LAN API URL used by Android: `http://10.64.241.153:8081/`
- Spring Boot PID listening on 8081: `35800`
- Spring Boot log: `D:\Studio\SpellBean\doyu-server\.run-output\springboot-dev.log`
- Docker Compose:
  - `douyu-postgres`: running and healthy, port `5433->5432`
  - `douyu-redis`: running and healthy, port `6379->6379`
- Android app PID after final launch: `11978`

## Open Notes

- This stage completed the community page and its detail click-through only.
  The remaining pages should be handled one by one in later stages.
- The imported dev content is explicit test/development data. It is not a
  backend startup seed and will disappear if the dev volume is rebuilt again
  unless the import script is rerun.
