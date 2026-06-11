package cn.edu.app.douyu.ui;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
/**
 * 列表页基类：封装 XML 列表页常用的加载、空态和错误态。
 */

public abstract class BaseListFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SummaryAdapter adapter;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;

    protected abstract String screenTitle();

    protected abstract String screenSubtitle();

    protected abstract String emptyText();

    protected abstract boolean grid();

    protected abstract List<SummaryItem> loadItems(DoyuRepository repository) throws Exception;

    protected abstract int layoutRes();

    protected void onSummaryClick(SummaryItem item) {
    }

    protected String[] chips() {
        return new String[0];
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutRes(), container, false);
        ((TextView) view.findViewById(R.id.section_title)).setText(screenTitle());
        ((TextView) view.findViewById(R.id.section_subtitle)).setText(screenSubtitle());
        ChipGroup chipGroup = view.findViewById(R.id.section_chips);
        String[] chipTexts = chips();
        for (int index = 0; index < chipTexts.length; index++) {
            String chipText = chipTexts[index];
            Chip chip = new Chip(requireContext());
            chip.setText(chipText);
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
        chipGroup.setVisibility(chipGroup.getChildCount() == 0 ? View.GONE : View.VISIBLE);

        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);
        adapter = new SummaryAdapter(this::onSummaryClick);
        adapter.setMasonry(grid());
        list.setLayoutManager(grid() ? new GridLayoutManager(requireContext(), 2) : new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        retry.setOnClickListener(v -> load());
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    protected final void load() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = ((DoyuApplication) requireActivity().getApplication()).repository();
        executor.execute(() -> {
            try {
                List<SummaryItem> items = loadItems(repository);
                Activity activity = getActivity();
                if (activity == null || !isAdded()) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    if (!isAdded() || adapter == null) {
                        return;
                    }
                    if (items == null || items.isEmpty()) {
                        show(LoadState.EMPTY, null);
                    } else {
                        adapter.submit(items);
                        show(LoadState.CONTENT, null);
                    }
                });
            } catch (Exception e) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    show(LoadState.from(e), e.getMessage());
                });
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
        if (errorBox != null) {
            errorBox.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        }
        error.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        retry.setVisibility(state == LoadState.ERROR ? View.VISIBLE : View.GONE);
        empty.setText(emptyText());
        error.setText(userFacingError(state, message));
        retry.setText(UiCopy.RETRY);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
}
