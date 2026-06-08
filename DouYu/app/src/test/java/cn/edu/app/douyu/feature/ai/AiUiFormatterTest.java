package cn.edu.app.douyu.feature.ai;

import org.junit.Test;

import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AiUiFormatterTest {
    @Test
    public void formatsKnownJobStatusesForAiDetail() {
        assertEquals("排队中", AiUiFormatter.statusLabel("PENDING"));
        assertEquals("生成中", AiUiFormatter.statusLabel("PROCESSING"));
        assertEquals("已完成", AiUiFormatter.statusLabel("SUCCEEDED"));
        assertEquals("生成失败", AiUiFormatter.statusLabel("FAILED"));
        assertEquals("已取消", AiUiFormatter.statusLabel("CANCELED"));
        assertEquals("未知状态", AiUiFormatter.statusLabel(""));
    }

    @Test
    public void progressPercentUsesBackendProgressAndStatusFallbacks() {
        assertEquals(62, AiUiFormatter.progressPercent(0.62d, "PROCESSING"));
        assertEquals(0, AiUiFormatter.progressPercent(null, "PENDING"));
        assertEquals(50, AiUiFormatter.progressPercent(null, "PROCESSING"));
        assertEquals(100, AiUiFormatter.progressPercent(null, "SUCCEEDED"));
        assertEquals(0, AiUiFormatter.progressPercent(-0.4d, "PROCESSING"));
        assertEquals(100, AiUiFormatter.progressPercent(1.8d, "PROCESSING"));
    }

    @Test
    public void onlyPendingAndProcessingJobsCanBeCanceled() {
        assertTrue(AiUiFormatter.canCancel("PENDING"));
        assertTrue(AiUiFormatter.canCancel("PROCESSING"));
        assertFalse(AiUiFormatter.canCancel("SUCCEEDED"));
        assertFalse(AiUiFormatter.canCancel("FAILED"));
        assertFalse(AiUiFormatter.canCancel("CANCELED"));
    }

    @Test
    public void patternIsActionableOnlyWhenPatternIdExists() {
        PatternJob job = new PatternJob();
        assertFalse(AiUiFormatter.hasPattern(job));

        job.patternId = "pattern_1";
        assertTrue(AiUiFormatter.hasPattern(job));

        PatternAsset asset = new PatternAsset();
        assertFalse(AiUiFormatter.canFavorite(asset));
        asset.patternId = "pattern_2";
        assertTrue(AiUiFormatter.canFavorite(asset));
    }

    @Test
    public void failureReasonFallsBackToReadableBoundaryCopy() {
        assertEquals("图片过暗", AiUiFormatter.failureReason("图片过暗"));
        assertEquals("后端未返回失败原因，请返回后重试或重新选择图片。", AiUiFormatter.failureReason(""));
        assertEquals("后端未返回失败原因，请返回后重试或重新选择图片。", AiUiFormatter.failureReason(null));
    }
}
