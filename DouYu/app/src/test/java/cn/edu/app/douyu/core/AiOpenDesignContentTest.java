package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class AiOpenDesignContentTest {
    @Test
    public void aiHomeLayoutContainsOpenDesignCreationAndHistorySurfaces() throws IOException {
        String xml = readLayout("fragment_ai_home.xml");

        assertContains(xml, "@+id/ai_album_upload");
        assertContains(xml, "@+id/ai_camera_entry");
        assertContains(xml, "@+id/ai_current_task_card");
        assertContains(xml, "@+id/ai_current_task_status");
        assertContains(xml, "@+id/ai_current_task_progress");
        assertContains(xml, "@+id/ai_history_title");
        assertContains(xml, "@+id/summary_list");
        assertContains(xml, "相册上传");
        assertContains(xml, "拍照");
        assertContains(xml, "历史图纸");
        assertContains(xml, "开发态工具");
    }

    @Test
    public void aiFlowLayoutContainsDetailParametersActionsAndDisabledBoundaries() throws IOException {
        String xml = readLayout("activity_ai_flow.xml");

        assertContains(xml, "@+id/ai_input_card");
        assertContains(xml, "@+id/ai_parameter_card");
        assertContains(xml, "@+id/ai_status_card");
        assertContains(xml, "@+id/ai_result_card");
        assertContains(xml, "@+id/ai_create_job");
        assertContains(xml, "@+id/ai_cancel_job");
        assertContains(xml, "@+id/ai_favorite_pattern");
        assertContains(xml, "@+id/ai_pdf_disabled");
        assertContains(xml, "@+id/ai_materials_disabled");
        assertContains(xml, "@+id/ai_share_disabled");
        assertContains(xml, "参数");
        assertContains(xml, "任务状态");
        assertContains(xml, "图纸详情");
        assertContains(xml, "PDF 待闭环");
        assertContains(xml, "材料加购待闭环");
        assertContains(xml, "分享到社区待闭环");
    }

    private static String readLayout(String name) throws IOException {
        return new String(Files.readAllBytes(Path.of("src/main/res/layout", name)), StandardCharsets.UTF_8);
    }

    private static void assertContains(String xml, String expected) {
        assertTrue("Missing " + expected, xml.contains(expected));
    }
}
