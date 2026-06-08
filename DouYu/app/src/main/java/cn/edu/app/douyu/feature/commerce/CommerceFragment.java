package cn.edu.app.douyu.feature.commerce;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductCategory;
import cn.edu.app.douyu.ui.LoadState;

public class CommerceFragment extends Fragment {
    private static final String FEATURED_ID = "";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ProductCategory> categories = new ArrayList<>();
    private CommerceProductAdapter adapter;
    private ChipGroup chipGroup;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;
    private String selectedCategoryId = FEATURED_ID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commerce_home, container, false);
        chipGroup = view.findViewById(R.id.section_chips);
        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);

        adapter = new CommerceProductAdapter(this::openProduct);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(adapter);
        list.setItemAnimator(null);
        retry.setOnClickListener(v -> loadProducts());

        renderChips();
        loadCategoriesAndProducts();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        chipGroup = null;
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

    private void loadCategoriesAndProducts() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        executor.execute(() -> {
            List<ProductCategory> loadedCategories = new ArrayList<>();
            try {
                PageResponse<ProductCategory> page = repository.productCategories();
                if (page != null && page.items != null) {
                    loadedCategories.addAll(page.items);
                }
            } catch (Exception ignored) {
                // Categories are optional for the first paint; the featured product list still uses real backend data.
            }
            try {
                PageResponse<Product> products = repository.products();
                runOnUi(() -> {
                    categories.clear();
                    categories.addAll(loadedCategories);
                    selectedCategoryId = FEATURED_ID;
                    renderChips();
                    renderProducts(products == null ? null : products.items);
                });
            } catch (Exception e) {
                runOnUi(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    private void loadProducts() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        String categoryId = selectedCategoryId;
        executor.execute(() -> {
            try {
                PageResponse<Product> page = repository.products(categoryId);
                runOnUi(() -> renderProducts(page == null ? null : page.items));
            } catch (Exception e) {
                runOnUi(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    private void renderProducts(List<Product> products) {
        if (adapter == null) {
            return;
        }
        if (products == null || products.isEmpty()) {
            adapter.submit(List.of());
            show(LoadState.EMPTY, null);
            return;
        }
        adapter.submit(products);
        show(LoadState.CONTENT, null);
    }

    private void renderChips() {
        if (chipGroup == null) {
            return;
        }
        chipGroup.removeAllViews();
        addChip("精选", FEATURED_ID);
        for (ProductCategory category : categories) {
            if (category.categoryId != null && category.name != null && !category.name.isBlank()) {
                addChip(category.name, category.categoryId);
            }
        }
        chipGroup.setVisibility(View.VISIBLE);
    }

    private void addChip(String text, String categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(false);
        chip.setTextSize(12);
        chip.setMinHeight(dp(30));
        chip.setChipCornerRadius(dp(15));
        chip.setChipStrokeWidth(dp(1));
        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_open_line)));
        boolean selected = categoryId.equals(selectedCategoryId);
        if (selected) {
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_petal_deep)));
        } else {
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.doyu_text_muted));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_surface)));
        }
        chip.setOnClickListener(v -> {
            if (categoryId.equals(selectedCategoryId)) {
                return;
            }
            selectedCategoryId = categoryId;
            renderChips();
            loadProducts();
        });
        chipGroup.addView(chip);
    }

    private void openProduct(Product product) {
        if (product == null || product.productId == null || product.productId.isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(IntentExtras.PRODUCT_ID, product.productId);
        startActivity(intent);
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
        empty.setText(emptyText());
        error.setText(userFacingError(state, message));
        retry.setText(UiCopy.RETRY);
    }

    private String emptyText() {
        if (selectedCategoryId == null || selectedCategoryId.isEmpty()) {
            return "暂无上架商品。商城只展示后端返回的真实商品，不使用本地假数据。";
        }
        return "这个分类还没有上架商品。分类只展示后端返回的真实商品。";
    }

    private String userFacingError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return "登录后才能查看这部分商城数据。当前不使用本地假商品填充页面。";
        }
        if (message == null || message.isEmpty()) {
            return "加载失败：服务暂时不可用，请稍后重试。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return "加载失败：暂时无法连接服务。页面保留真实错误状态，不使用本地假商品。";
        }
        return "加载失败：" + message;
    }

    private DoyuRepository repository() {
        return ((DoyuApplication) requireActivity().getApplication()).repository();
    }

    private void runOnUi(Runnable action) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
