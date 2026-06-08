# 2026-06-08 Commerce Product Image Sources

## Scope

This file records reusable-license image sources selected for the commerce real products repair. The imported images are one-time dev verification product covers and are not runtime seed content.

Source policy:

- Wikimedia Commons Thermobeads was checked first as the preferred source.
- Commons API returned a temporary rate-limit response during this run, so Openverse CC results were used to complete the set.
- Images with obvious character IP or unclear reuse terms were excluded.
- The import script uploaded the downloaded files through the app upload flow and recorded the resulting public OSS URLs.
- Two additional candidates were not imported because a Commons/download URL did not complete reliably; no unclear-license replacement image was used.

## Imported Images

| Product ID | Product title | Category | Source title | Author | License | Source URL | Actual size | OSS public URL |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| commerce_real_snowflake_kit | 雪花拼豆材料组合 | 套装 | Snowflakes with Hama Beads | petuniad | CC BY-SA | https://www.flickr.com/photos/25148303@N07/8425282626 | 1024x768 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_fd17e936175b417a8f7d3706858bc47b/01-commerce_real_snowflake_kit.jpg |
| commerce_real_mobile_hanger | 拼豆挂饰材料包 | 套装 | Hama Beads Mobile | petuniad | CC BY-SA | https://www.flickr.com/photos/25148303@N07/6893644437 | 768x1024 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_c6f6fda5b99c4dc7a217b93ddc16fa77/02-commerce_real_mobile_hanger.jpg |
| commerce_real_heart_edelweiss | 心形花纹拼豆补充包 | 豆子 | Hama Beads Heart Edelweiss | petuniad | CC BY-SA | https://www.flickr.com/photos/25148303@N07/8485836309 | 1024x768 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_3ca60707c11e47bbbe0cf04199c0dab3/03-commerce_real_heart_edelweiss.jpg |
| commerce_real_flower_mobile | 花朵挂件拼豆套装 | 套装 | Hama Beads Mobile with Flowers | petuniad | CC BY-SA | https://www.flickr.com/photos/25148303@N07/6893644287 | 768x1024 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_e9e74956c62b40c29f5d1307912f908c/04-commerce_real_flower_mobile.jpg |
| commerce_real_desk_beads | 桌面混色拼豆盒 | 豆子 | Hama beads on my desk | humbert15 | CC BY | https://www.flickr.com/photos/27532236@N00/5573207625 | 1000x664 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_63c2a801aad849db93d0fbed3e32ca66/05-commerce_real_desk_beads.jpg |
| commerce_real_workshop_pack | 拼豆工作坊材料包 | 工具 | Hama beads workshop | lespounder | CC BY-SA | https://www.flickr.com/photos/45703688@N07/6860863265 | 1023x612 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_886978fb49fe418292da2b0445b0d5e2/06-commerce_real_workshop_pack.jpg |
| commerce_real_color_beads | 彩色拼豆补充袋 | 豆子 | Hama beads | lespounder | CC BY-SA | https://www.flickr.com/photos/45703688@N07/6860860793 | 1023x612 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_57b3bb810ae74484b1cb6558d6b80f79/07-commerce_real_color_beads.jpg |
| commerce_real_storage_beads | 玩家闲置拼豆收纳盒 | 玩家 | Hama beads | lespounder | CC BY-SA | https://www.flickr.com/photos/45703688@N07/6860861805 | 1023x612 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_e11b1802f402401ca9a15a3ce4113775/08-commerce_real_storage_beads.jpg |
| commerce_real_taller_tools | 拼豆工具与操作垫 | 工具 | Taller Hama Beads | jmerelo | CC BY-SA | https://www.flickr.com/photos/48600078653@N01/7328617634 | 768x1024 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_ced189952cf8448f812641ac5715fab6/09-commerce_real_taller_tools.jpg |
| commerce_real_heart_pegboard | 心形拼豆模板板 | 板子 | Perler Bead Heart Pegboard | Andy Forest | CC0 | https://www.thingiverse.com/thing:3577056 | 800x600 | https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/stub/product_image/file_a17e36e701d9437cb1a73249cc87cbb4/10-commerce_real_heart_pegboard.jpg |

## Not Imported Candidates

| Product ID | Product title | Source title | Reason |
| --- | --- | --- | --- |
| commerce_real_heart_cover | 心形拼豆成品参考 | File:Perler bead heart beaded side.jpg | Download did not complete reliably during this run. Not replaced with unclear-license media. |
| commerce_real_tray_beads | 拼豆托盘收纳套件 | Tray of Beads | Download did not complete reliably during this run. Not replaced with unclear-license media. |

## Evidence

- Import report: `doyu-server/.qa-output/commerce-products-import-report.json`
- Local downloads: `doyu-server/.qa-output/commerce-products/`
- Android screenshots: `doc/development/verification/android-java-xml-screenshots/2026-06-08-commerce-real-products/`
