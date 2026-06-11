package cn.edu.app.douyu.feature.community;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.feature.commerce.ProductDetailActivity;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.SearchResult;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class SearchActivity extends XmlPageActivity {
    private static final long SEARCH_DEBOUNCE_MS = 300L;
    private static final String TYPE_ALL = "all";
    private static final String TYPE_POSTS = "posts";
    private static final String TYPE_PRODUCTS = "products";
    private static final String TYPE_USERS = "users";
    private static final String TYPE_TOPICS = "topics";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int requestVersion = 0;
    private String currentType = TYPE_ALL;

    private EditText searchInput;
    private TextView clear;
    private TextView empty;
    private TextView errorText;
    private View errorBox;
    private ProgressBar loading;
    private RecyclerView results;
    private SearchResultAdapter adapter;
    private TextView tabAll;
    private TextView tabPosts;
    private TextView tabProducts;
    private TextView tabUsers;
    private TextView tabTopics;

    private final Runnable debouncedSearch = this::searchNow;

    @Override
    protected int layoutRes() {
        return R.layout.activity_search;
    }

    @Override
    protected String title() {
        return "搜索";
    }

    @Override
    protected void bindViews() {
        searchInput = findViewById(R.id.search_input);
        clear = findViewById(R.id.search_clear);
        empty = findViewById(R.id.search_empty);
        errorText = findViewById(R.id.search_error_text);
        errorBox = findViewById(R.id.search_error_box);
        loading = findViewById(R.id.search_loading);
        results = findViewById(R.id.search_results);
        tabAll = findViewById(R.id.search_tab_all);
        tabPosts = findViewById(R.id.search_tab_posts);
        tabProducts = findViewById(R.id.search_tab_products);
        tabUsers = findViewById(R.id.search_tab_users);
        tabTopics = findViewById(R.id.search_tab_topics);

        adapter = new SearchResultAdapter(this::openResult);
        results.setLayoutManager(new LinearLayoutManager(this));
        results.setAdapter(adapter);

        findViewById(R.id.search_cancel).setOnClickListener(v -> finish());
        clear.setOnClickListener(v -> searchInput.setText(""));
        findViewById(R.id.search_retry).setOnClickListener(v -> searchNow());
        bindTab(tabAll, TYPE_ALL);
        bindTab(tabPosts, TYPE_POSTS);
        bindTab(tabProducts, TYPE_PRODUCTS);
        bindTab(tabUsers, TYPE_USERS);
        bindTab(tabTopics, TYPE_TOPICS);
        bindInput();

        updateTabs();
        showEmpty("输入关键词搜索作品、商品、用户和话题。");
        searchInput.requestFocus();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(debouncedSearch);
        super.onDestroy();
    }

    private void bindInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clear.setVisibility(trim(s).isEmpty() ? View.GONE : View.VISIBLE);
                scheduleSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchInput.setOnEditorActionListener((view, actionId, event) -> {
            boolean keyboardSearch = actionId == EditorInfo.IME_ACTION_SEARCH;
            boolean enterKey = event != null
                    && event.getAction() == KeyEvent.ACTION_UP
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (keyboardSearch || enterKey) {
                handler.removeCallbacks(debouncedSearch);
                searchNow();
                return true;
            }
            return false;
        });
    }

    private void bindTab(TextView tab, String type) {
        tab.setOnClickListener(v -> {
            if (!currentType.equals(type)) {
                currentType = type;
                updateTabs();
                handler.removeCallbacks(debouncedSearch);
                searchNow();
            }
        });
    }

    private void scheduleSearch() {
        handler.removeCallbacks(debouncedSearch);
        handler.postDelayed(debouncedSearch, SEARCH_DEBOUNCE_MS);
    }

    private void searchNow() {
        String keyword = trim(searchInput.getText());
        int version = ++requestVersion;
        if (keyword.isEmpty()) {
            adapter.submit(null);
            showEmpty("输入关键词搜索作品、商品、用户和话题。");
            return;
        }
        showLoading();
        loadDetail(
                repository -> repository.search(keyword, currentType),
                page -> {
                    if (version == requestVersion) {
                        renderResults(keyword, page);
                    }
                },
                (state, message) -> {
                    if (version == requestVersion) {
                        showError(message);
                    }
                });
    }

    private void renderResults(String keyword, PageResponse<SearchResult> page) {
        if (page == null || page.isEmpty()) {
            adapter.submit(null);
            showEmpty("没有找到与“" + keyword + "”相关的结果。");
            return;
        }
        adapter.submit(page.items);
        loading.setVisibility(View.GONE);
        errorBox.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        results.setVisibility(View.VISIBLE);
    }

    private void openResult(SearchResult result) {
        if (result == null || result.targetId == null || result.targetId.trim().isEmpty()) {
            return;
        }
        if (SearchResult.TYPE_POST.equals(result.resultType)) {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra(IntentExtras.POST_ID, result.targetId);
            startActivity(intent);
            return;
        }
        if (SearchResult.TYPE_PRODUCT.equals(result.resultType)) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(IntentExtras.PRODUCT_ID, result.targetId);
            startActivity(intent);
            return;
        }
        if (SearchResult.TYPE_TOPIC.equals(result.resultType)) {
            currentType = TYPE_POSTS;
            updateTabs();
            searchInput.setText(nonBlank(result.title, result.targetId));
            searchInput.setSelection(searchInput.getText().length());
            handler.removeCallbacks(debouncedSearch);
            searchNow();
            return;
        }
        if (SearchResult.TYPE_USER.equals(result.resultType)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(nonBlank(result.title, "用户"))
                    .setMessage(nonBlank(result.subtitle, "该用户来自后端搜索结果。"))
                    .setPositiveButton("知道了", null)
                    .show();
        }
    }

    private void updateTabs() {
        setTab(tabAll, TYPE_ALL.equals(currentType));
        setTab(tabPosts, TYPE_POSTS.equals(currentType));
        setTab(tabProducts, TYPE_PRODUCTS.equals(currentType));
        setTab(tabUsers, TYPE_USERS.equals(currentType));
        setTab(tabTopics, TYPE_TOPICS.equals(currentType));
    }

    private void setTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_plain);
        tab.setTextColor(getColor(selected ? R.color.white : R.color.doyu_text_muted));
    }

    private void showLoading() {
        loading.setVisibility(View.VISIBLE);
        results.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        errorBox.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        loading.setVisibility(View.GONE);
        results.setVisibility(View.GONE);
        errorBox.setVisibility(View.GONE);
        empty.setText(message);
        empty.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        loading.setVisibility(View.GONE);
        results.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        errorText.setText(nonBlank(message, "搜索失败，请稍后重试。"));
        errorBox.setVisibility(View.VISIBLE);
    }

    private static String trim(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
