package cn.edu.app.douyu.core

import cn.edu.app.douyu.feature.ai.aiHomeTopBarTitle
import cn.edu.app.douyu.feature.ai.aiHomeUploadActionLabels
import cn.edu.app.douyu.feature.ai.aiHomeDevelopmentBoundaryCopy
import cn.edu.app.douyu.feature.ai.aiHomeHeroBoundaryCopy
import cn.edu.app.douyu.feature.ai.aiHistoryEmptyStateCopy
import cn.edu.app.douyu.feature.ai.aiImagePreviewActionLabels
import cn.edu.app.douyu.feature.ai.aiJobStatusLabel
import cn.edu.app.douyu.feature.ai.aiResultDisabledFeatureCopy
import cn.edu.app.douyu.feature.ai.aiUploadFailureMessage
import cn.edu.app.douyu.feature.commerce.commerceHomeCategoryLabels
import cn.edu.app.douyu.feature.commerce.commerceHomeHeroCopy
import cn.edu.app.douyu.feature.commerce.commerceHomePrimarySectionOrder
import cn.edu.app.douyu.feature.commerce.orderConfirmCtaLabel
import cn.edu.app.douyu.feature.commerce.paymentProviderBoundaryMessage
import cn.edu.app.douyu.feature.commerce.productCanUseStandardCart
import cn.edu.app.douyu.core.model.ProductType
import cn.edu.app.douyu.feature.community.communityHomeChannelLabels
import cn.edu.app.douyu.feature.community.communityHomeHeroCopy
import cn.edu.app.douyu.feature.community.communityHomeTopBarTitle
import cn.edu.app.douyu.feature.community.postComposePrimarySections
import cn.edu.app.douyu.feature.community.postComposeStateLabels
import cn.edu.app.douyu.feature.community.postComposeTopBarTitle
import cn.edu.app.douyu.feature.community.searchDisabledResultTrailingLabel
import cn.edu.app.douyu.feature.community.searchPatternScopeBoundaryCopy
import cn.edu.app.douyu.feature.message.conversationStatusLabel
import cn.edu.app.douyu.feature.message.messageHomeHeroCopy
import cn.edu.app.douyu.feature.message.messageHomePreviewSectionLabels
import cn.edu.app.douyu.feature.profile.profileEditAvatarStateLabels
import cn.edu.app.douyu.feature.profile.profileEditPrimaryFields
import cn.edu.app.douyu.feature.profile.profileAssetPreviewBadges
import cn.edu.app.douyu.feature.profile.profileAssetTabLabels
import cn.edu.app.douyu.feature.profile.profilePrimaryAssetBoundaryMessage
import cn.edu.app.douyu.feature.profile.settingsHomeSectionLabels
import cn.edu.app.douyu.core.model.PatternJobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StageEightUiCopyRuleTest {
    @Test
    fun aiHomeTitleFollowsOpenDesignCopy() {
        assertEquals("AI 创作", aiHomeTopBarTitle())
    }

    @Test
    fun aiHomeExposesOpenDesignUploadActions() {
        assertEquals(listOf("相册上传", "拍照"), aiHomeUploadActionLabels())
    }

    @Test
    fun aiHomeKeepsDevelopmentProviderBoundaryVisible() {
        assertEquals(
            listOf(
                "开发态 AI 能力",
                "当前后端仍是开发态图纸生成链路，真实视觉 Provider 和大模型生图 API 未接入；结果只用于联调和 UI 验收。"
            ),
            aiHomeDevelopmentBoundaryCopy()
        )
        assertEquals("生成质量以后端 Provider 能力为准，当前不承诺真实视觉理解。", aiHomeHeroBoundaryCopy())
    }

    @Test
    fun aiJobStatusLabelsCoverQueueProgressAndTerminalStates() {
        assertEquals("排队中", aiJobStatusLabel(PatternJobStatus.PENDING))
        assertEquals("处理中", aiJobStatusLabel(PatternJobStatus.PROCESSING))
        assertEquals("已完成", aiJobStatusLabel(PatternJobStatus.SUCCEEDED))
        assertEquals("失败", aiJobStatusLabel(PatternJobStatus.FAILED))
        assertEquals("失败", aiJobStatusLabel(PatternJobStatus.REJECTED))
        assertEquals("已取消", aiJobStatusLabel(PatternJobStatus.CANCELED))
    }

    @Test
    fun aiResultDoesNotExposeFutureMaterialActionsAsRealCapabilities() {
        assertEquals(
            listOf(
                "材料购买待接入",
                "图纸材料清单已展示，自动加购、PDF 导出和带图纸发帖将在真实链路接入后开放。"
            ),
            aiResultDisabledFeatureCopy()
        )
    }

    @Test
    fun aiHistoryEmptySuccessStillShowsEmptyStateCopy() {
        assertEquals(
            listOf(
                "还没有生成过图纸",
                "选择一张图片，开始生成你的第一张拼豆图纸吧！"
            ),
            aiHistoryEmptyStateCopy()
        )
    }

    @Test
    fun aiUploadFailureMessageKeepsQaReasonVisibleWithoutLeakingUrls() {
        assertEquals(
            "上传失败：java.io.IOException: Upload failed: 403 Forbidden",
            aiUploadFailureMessage(java.io.IOException("Upload failed: 403 Forbidden"))
        )
        assertEquals(
            "上传失败：java.io.IOException: https://<redacted>",
            aiUploadFailureMessage(java.io.IOException("https://example.com/upload?signature=secret"))
        )
        assertEquals(
            "上传失败：请检查网络后重试",
            aiUploadFailureMessage(RuntimeException())
        )
    }

    @Test
    fun aiCameraPreviewKeepsRotateUploadCancelAndRetakeActions() {
        assertEquals(
            listOf("左转", "右转", "取消", "重拍", "上传并继续", "重试上传"),
            aiImagePreviewActionLabels(fromCamera = true)
        )
        assertEquals(
            listOf("左转", "右转", "取消", "重新选择", "上传并继续", "重试上传"),
            aiImagePreviewActionLabels(fromCamera = false)
        )
    }

    @Test
    fun communityHomeTitleFollowsOpenDesignCopy() {
        assertEquals("社区", communityHomeTopBarTitle())
    }

    @Test
    fun communityHomeChannelsFollowOpenDesignCopy() {
        assertEquals(listOf("推荐", "关注", "教程", "图纸", "新手"), communityHomeChannelLabels())
    }

    @Test
    fun communityHomeHeroFollowsOpenDesignCopy() {
        assertEquals(
            listOf("今日灵感", "真实作品图优先展示", "图片失败时保持卡片宽度与高度上限，回退拼豆色块占位。"),
            communityHomeHeroCopy()
        )
    }

    @Test
    fun postComposeFollowsOpenDesignStructure() {
        assertEquals("上传帖子", postComposeTopBarTitle())
        assertEquals(listOf("图片", "正文", "话题", "审核前预览"), postComposePrimarySections())
        assertEquals(listOf("上传中", "上传失败", "9/9", "审核中"), postComposeStateLabels())
    }

    @Test
    fun commerceHomeCategoriesFollowOpenDesignCopy() {
        assertEquals(listOf("精选", "豆子", "板子", "工具", "玩家"), commerceHomeCategoryLabels())
    }

    @Test
    fun commerceHomeHeroFollowsOpenDesignCopy() {
        assertEquals(
            listOf("自营精选", "新手材料补给", "只展示真实可解释活动，点击路径必须存在。"),
            commerceHomeHeroCopy()
        )
    }

    @Test
    fun commerceHomePrimarySectionsFollowOpenDesignOrder() {
        assertEquals(listOf("分类", "Banner", "商品卡片"), commerceHomePrimarySectionOrder())
    }

    @Test
    fun commerceBoundaryRulesKeepPlayerGoodsAndStubPaymentHonest() {
        assertEquals(true, productCanUseStandardCart(ProductType.SELF_OPERATED))
        assertEquals(false, productCanUseStandardCart(ProductType.PLAYER_SECOND_HAND))
        assertEquals(false, productCanUseStandardCart(ProductType.PLAYER_CUSTOM_SERVICE))
        assertEquals("缺少收货地址 · ¥12.80", orderConfirmCtaLabel(hasAddress = false, amountCent = 1280))
        assertEquals(
            "payParams.provider=STUB，不代表真实微信或支付宝收款。",
            paymentProviderBoundaryMessage("STUB")
        )
    }

    @Test
    fun messageHomeFirstScreenFollowsOpenDesignSections() {
        assertEquals(
            listOf("新消息进入私信", "未读对话", "对话输入中、未互关剩余条数、禁发态只在私信里展示。"),
            messageHomeHeroCopy()
        )
        assertEquals(listOf("未读对话", "通知预览"), messageHomePreviewSectionLabels())
    }

    @Test
    fun conversationStatusBadgesFollowOpenDesignCopy() {
        assertEquals("互关", conversationStatusLabel(mutualFollow = true, remaining = 0, canSend = true))
        assertEquals("1/3", conversationStatusLabel(mutualFollow = false, remaining = 1, canSend = true))
        assertEquals("禁发", conversationStatusLabel(mutualFollow = false, remaining = 0, canSend = false))
    }

    @Test
    fun searchRowsWithoutDestinationShowUiOnlyTrailingLabel() {
        assertEquals("UI-only", searchDisabledResultTrailingLabel())
    }

    @Test
    fun searchPatternScopeDoesNotReusePostResultCopy() {
        assertEquals(
            listOf("图纸搜索待接入", "当前没有全局图纸搜索后端；可先查看 AI 历史或我的图纸。"),
            searchPatternScopeBoundaryCopy()
        )
    }

    @Test
    fun profilePrimaryAssetTabsFollowOpenDesignAndHideRemovedEntries() {
        val labels = profileAssetTabLabels()

        assertEquals(listOf("我的图纸", "点赞作品", "收藏作品"), labels)
        assertFalse(labels.contains("我的订单"))
        assertFalse(labels.contains("订单记录"))
        assertFalse(labels.contains("评论作品"))
        assertFalse(labels.contains("关注作品"))
    }

    @Test
    fun profileAssetTabsUseUnifiedCardFlowBadges() {
        assertEquals(listOf("图纸", "帖子", "收藏"), profileAssetPreviewBadges())
    }

    @Test
    fun profileEditFollowsOpenDesignFieldsAndAvatarStates() {
        assertEquals(listOf("头像", "昵称", "个人简介", "年龄段", "城市 / 地区", "兴趣标签"), profileEditPrimaryFields())
        assertEquals(listOf("更换头像", "头像上传中", "头像上传失败", "保留旧头像"), profileEditAvatarStateLabels())
    }

    @Test
    fun settingsHomeExposesOpenDesignSections() {
        assertEquals(listOf("账号与安全", "隐私与权限", "通知设置", "帮助、关于与合规"), settingsHomeSectionLabels())
    }

    @Test
    fun profilePrimaryAssetBoundaryMessageDoesNotShowRemovedEntryNames() {
        val message = profilePrimaryAssetBoundaryMessage()

        assertFalse(message.contains("我的订单"))
        assertFalse(message.contains("订单记录"))
        assertFalse(message.contains("评论作品"))
        assertFalse(message.contains("关注作品"))
    }
}
