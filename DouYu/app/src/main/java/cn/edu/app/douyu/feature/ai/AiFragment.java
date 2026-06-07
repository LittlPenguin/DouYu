package cn.edu.app.douyu.feature.ai;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.ui.BaseListFragment;
import cn.edu.app.douyu.ui.SummaryItem;

public class AiFragment extends BaseListFragment {
    @Override
    protected int layoutRes() {
        return R.layout.fragment_ai_home;
    }

    @Override
    protected String screenTitle() {
        return "AI 图纸";
    }

    @Override
    protected String screenSubtitle() {
        return "图片选择、CameraX 拍照、参数、进度、结果和历史都以真实任务状态为准。";
    }

    @Override
    protected String emptyText() {
        return UiCopy.AI_EMPTY;
    }

    @Override
    protected boolean grid() {
        return false;
    }

    @Override
    protected String[] chips() {
        return new String[]{"上传", "拍照", "历史", "UI-only"};
    }

    @Override
    protected List<SummaryItem> loadItems(DoyuRepository repository) throws Exception {
        PageResponse<PatternJob> page = repository.patternJobs();
        List<SummaryItem> items = new ArrayList<>();
        if (page != null && page.items != null) {
            for (PatternJob job : page.items) {
                items.add(new SummaryItem(
                        job.jobId,
                        "任务 " + safe(job.jobId),
                        safe(job.status) + " | " + safe(job.createdAt),
                        job.sourceImageUrl
                ));
            }
        }
        return items;
    }

    @Override
    protected void onSummaryClick(SummaryItem item) {
        Intent intent = new Intent(requireContext(), AiFlowActivity.class);
        intent.putExtra(IntentExtras.JOB_ID, item.id);
        startActivity(intent);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
