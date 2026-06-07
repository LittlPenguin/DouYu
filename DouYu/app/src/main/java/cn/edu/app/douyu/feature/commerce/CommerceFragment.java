package cn.edu.app.douyu.feature.commerce;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.MoneyFormatter;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.ui.BaseListFragment;
import cn.edu.app.douyu.ui.SummaryItem;

public class CommerceFragment extends BaseListFragment {
    @Override
    protected int layoutRes() {
        return R.layout.fragment_commerce_home;
    }

    @Override
    protected String screenTitle() {
        return "商城";
    }

    @Override
    protected String screenSubtitle() {
        return "自营材料、玩家寄售和定制服务按接口状态展示；真实支付未接入前保持边界。";
    }

    @Override
    protected String emptyText() {
        return UiCopy.COMMERCE_EMPTY;
    }

    @Override
    protected boolean grid() {
        return true;
    }

    @Override
    protected String[] chips() {
        return new String[]{"自营", "二手", "定制", "材料"};
    }

    @Override
    protected List<SummaryItem> loadItems(DoyuRepository repository) throws Exception {
        PageResponse<Product> page = repository.products();
        List<SummaryItem> items = new ArrayList<>();
        if (page != null && page.items != null) {
            for (Product product : page.items) {
                String title = first(product.title, product.name, "未命名商品");
                items.add(new SummaryItem(
                        product.productId,
                        title,
                        MoneyFormatter.centsToYuan(product.priceCents) + " | " + safe(product.status),
                        product.imageUrl
                ));
            }
        }
        return items;
    }

    @Override
    protected void onSummaryClick(SummaryItem item) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(IntentExtras.PRODUCT_ID, item.id);
        startActivity(intent);
    }

    private static String first(String a, String b, String fallback) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        if (b != null && !b.isEmpty()) {
            return b;
        }
        return fallback;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
