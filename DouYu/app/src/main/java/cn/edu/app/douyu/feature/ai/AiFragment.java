package cn.edu.app.douyu.feature.ai;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.SummaryAdapter;
import cn.edu.app.douyu.ui.SummaryItem;

public class AiFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && getContext() != null) {
                    Intent intent = new Intent(requireContext(), AiFlowActivity.class);
                    intent.setData(uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            });

    private SummaryAdapter adapter;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;
    private TextView currentTitle;
    private TextView currentStatus;
    private TextView currentSubtitle;
    private ProgressBar currentProgress;
    private MaterialButton currentCancel;
    private MaterialButton currentOpen;
    private PatternJob activeTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_home, container, false);
        bindChips(view);
        bindViews(view);
        load();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        loading = null;
        list = null;
        empty = null;
        errorBox = null;
        error = null;
        retry = null;
        currentTitle = null;
        currentStatus = null;
        currentSubtitle = null;
        currentProgress = null;
        currentCancel = null;
        currentOpen = null;
        activeTask = null;
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void bindViews(View view) {
        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);
        currentTitle = view.findViewById(R.id.ai_current_task_title);
        currentStatus = view.findViewById(R.id.ai_current_task_status);
        currentSubtitle = view.findViewById(R.id.ai_current_task_subtitle);
        currentProgress = view.findViewById(R.id.ai_current_task_progress);
        currentCancel = view.findViewById(R.id.ai_current_task_cancel);
        currentOpen = view.findViewById(R.id.ai_current_task_open);

        adapter = new SummaryAdapter(this::openJob);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        view.findViewById(R.id.ai_album_upload).setOnClickListener(v -> pickImage.launch("image/*"));
        view.findViewById(R.id.ai_camera_entry).setOnClickListener(v -> startActivity(new Intent(requireContext(), CameraActivity.class)));
        retry.setOnClickListener(v -> load());
        currentOpen.setOnClickListener(v -> {
            if (activeTask != null) {
                openJob(activeTask.jobId);
            }
        });
        currentCancel.setOnClickListener(v -> cancelActiveTask());
        renderNoActiveTask();
    }

    private void bindChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.section_chips);
        String[] chipTexts = new String[]{"上传", "拍照", "历史", "UI-only"};
        for (int index = 0; index < chipTexts.length; index++) {
            Chip chip = new Chip(requireContext());
            chip.setText(chipTexts[index]);
            chip.setCheckable(false);
            chip.setTextSize(12);
            chip.setMinHeight(dp(30));
            chip.setChipCornerRadius(dp(15));
            chip.setChipStrokeWidth(dp(1));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_open_line)));
            if (index == 0) {
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_petal_deep)));
            } else {
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.doyu_text_muted));
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_surface)));
            }
            chipGroup.addView(chip);
        }
    }

    private void load() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = ((DoyuApplication) requireActivity().getApplication()).repository();
        executor.execute(() -> {
            try {
                PageResponse<PatternJob> page = repository.patternJobs();
                List<PatternJob> jobs = page == null || page.items == null ? new ArrayList<>() : page.items;
                Activity activity = getActivity();
                if (activity == null || !isAdded()) {
                    return;
                }
                activity.runOnUiThread(() -> renderJobs(jobs));
            } catch (Exception e) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) {
                    return;
                }
                activity.runOnUiThread(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    private void renderJobs(List<PatternJob> jobs) {
        if (!isAdded() || adapter == null) {
            return;
        }
        activeTask = firstActive(jobs);
        if (activeTask == null) {
            renderNoActiveTask();
        } else {
            renderActiveTask(activeTask);
        }
        List<SummaryItem> history = new ArrayList<>();
        for (PatternJob job : jobs) {
            if (job == null || job == activeTask) {
                continue;
            }
            history.add(toSummary(job));
        }
        adapter.submit(history);
        show(history.isEmpty() ? LoadState.EMPTY : LoadState.CONTENT, null);
    }

    private void renderNoActiveTask() {
        if (currentTitle == null) {
            return;
        }
        currentTitle.setText("当前任务");
        currentStatus.setText("IDLE");
        currentSubtitle.setText("当前没有生成中的任务。相册上传或拍照后可创建真实 AI 图纸任务。");
        currentProgress.setProgress(0);
        currentCancel.setEnabled(false);
        currentOpen.setEnabled(false);
    }

    private void renderActiveTask(PatternJob job) {
        int progress = AiUiFormatter.progressPercent(job.progress, job.status);
        currentTitle.setText("正在生成");
        currentStatus.setText(AiUiFormatter.valueOrFallback(job.status, "UNKNOWN"));
        currentSubtitle.setText(titleFor(job) + " · 约 " + progress + "% · " + AiUiFormatter.statusLabel(job.status));
        currentProgress.setProgress(progress);
        currentCancel.setEnabled(AiUiFormatter.canCancel(job.status));
        currentOpen.setEnabled(job.jobId != null && !job.jobId.isEmpty());
    }

    private PatternJob firstActive(List<PatternJob> jobs) {
        for (PatternJob job : jobs) {
            if (job != null && AiUiFormatter.canCancel(job.status)) {
                return job;
            }
        }
        return null;
    }

    private SummaryItem toSummary(PatternJob job) {
        String subtitle = AiUiFormatter.statusLabel(job.status)
                + " | " + AiUiFormatter.valueOrFallback(job.beadSize, "未知规格")
                + " | " + AiUiFormatter.valueOrFallback(job.targetSize, "未知尺寸");
        return new SummaryItem(
                AiUiFormatter.valueOrFallback(job.jobId, ""),
                titleFor(job),
                subtitle,
                job.sourceImageUrl
        );
    }

    private String titleFor(PatternJob job) {
        return AiUiFormatter.valueOrFallback(job.inputName,
                "AI 图纸任务 " + AiUiFormatter.valueOrFallback(job.jobId, "未知"));
    }

    private void openJob(SummaryItem item) {
        openJob(item.id);
    }

    private void openJob(String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), AiFlowActivity.class);
        intent.putExtra(IntentExtras.JOB_ID, jobId);
        startActivity(intent);
    }

    private void cancelActiveTask() {
        if (activeTask == null || activeTask.jobId == null || activeTask.jobId.isEmpty()) {
            return;
        }
        String jobId = activeTask.jobId;
        currentCancel.setEnabled(false);
        currentSubtitle.setText("正在取消任务：" + jobId);
        DoyuRepository repository = ((DoyuApplication) requireActivity().getApplication()).repository();
        executor.execute(() -> {
            try {
                repository.cancelPatternJob(jobId);
                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    activity.runOnUiThread(this::load);
                }
            } catch (Exception e) {
                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    activity.runOnUiThread(() -> show(LoadState.from(e), e.getMessage()));
                }
            }
        });
    }

    private void show(LoadState state, String message) {
        if (loading == null) {
            return;
        }
        loading.setVisibility(state == LoadState.LOADING ? View.VISIBLE : View.GONE);
        list.setVisibility(state == LoadState.CONTENT ? View.VISIBLE : View.GONE);
        empty.setVisibility(state == LoadState.EMPTY ? View.VISIBLE : View.GONE);
        errorBox.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        error.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        retry.setVisibility(state == LoadState.ERROR ? View.VISIBLE : View.GONE);
        empty.setText(UiCopy.AI_EMPTY);
        error.setText(userFacingError(state, message));
        retry.setText(UiCopy.RETRY);
        if (state == LoadState.LOGIN_REQUIRED || state == LoadState.ERROR) {
            renderNoActiveTask();
        }
    }

    private String userFacingError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "服务暂不可用，请稍后重试。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "暂时无法连接服务。页面保留真实错误态，不使用本地假内容。";
        }
        return UiCopy.ERROR_PREFIX + message;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
