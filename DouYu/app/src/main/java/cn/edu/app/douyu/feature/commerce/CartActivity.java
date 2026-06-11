package cn.edu.app.douyu.feature.commerce;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.core.MoneyFormatter;
import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Order;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class CartActivity extends XmlPageActivity {
    private final CartAdapter adapter = new CartAdapter(new CartAdapter.Listener() {
        @Override
        public void onSelectionChanged(CartResponse.Item item, boolean selected) {
            selectItem(item, selected);
        }

        @Override
        public void onQuantityDelta(CartResponse.Item item, int delta) {
            updateQuantity(item, delta);
        }

        @Override
        public void onDelete(CartResponse.Item item) {
            deleteItem(item);
        }
    });
    private final Set<String> selectedItemIds = new HashSet<>();
    private final List<CartResponse.Item> currentItems = new ArrayList<>();

    private RecyclerView cartList;
    private ProgressBar loading;
    private TextView emptyText;
    private TextView cartStatus;
    private TextView cartTotal;
    private MaterialButton checkoutButton;
    private MaterialButton retryButton;
    private EditText recipientInput;
    private EditText phoneInput;
    private EditText regionInput;
    private EditText detailInput;
    private EditText remarkInput;
    private boolean busy;
    private boolean defaultSelectionApplied;
    private String pendingSuccessStatus;
    private Runnable pendingAfterLogin;
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected int layoutRes() {
        return R.layout.activity_cart;
    }

    @Override
    protected String title() {
        return "购物车";
    }

    @Override
    protected void bindViews() {
        bindFields();
        registerLoginLauncher();
        bindActions();
        renderState(LoadState.LOADING, "正在加载购物车...");
        if (!requireLoggedIn(AuthGate.RETURN_ACTION_CART, this::loadCart)) {
            renderState(LoadState.LOGIN_REQUIRED, "请先登录后查看购物车。");
        }
    }

    private void bindFields() {
        cartList = findViewById(R.id.cart_list);
        loading = findViewById(R.id.cart_loading);
        emptyText = findViewById(R.id.cart_empty);
        cartStatus = findViewById(R.id.cart_status);
        cartTotal = findViewById(R.id.cart_total);
        checkoutButton = findViewById(R.id.cart_checkout_button);
        retryButton = findViewById(R.id.cart_retry_button);
        recipientInput = findViewById(R.id.cart_address_recipient_input);
        phoneInput = findViewById(R.id.cart_address_phone_input);
        regionInput = findViewById(R.id.cart_address_region_input);
        detailInput = findViewById(R.id.cart_address_detail_input);
        remarkInput = findViewById(R.id.cart_order_remark_input);

        cartList.setLayoutManager(new LinearLayoutManager(this));
        cartList.setAdapter(adapter);
        cartList.setItemAnimator(null);
    }

    private void registerLoginLauncher() {
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) {
                renderState(LoadState.LOGIN_REQUIRED, "请先登录后查看购物车。");
                return;
            }
            Runnable pending = pendingAfterLogin;
            pendingAfterLogin = null;
            String action = result.getData() == null
                    ? ""
                    : result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION);
            if (pending != null && AuthGate.RETURN_ACTION_CART.equals(action)) {
                pending.run();
            } else {
                loadCart();
            }
        });
    }

    private void bindActions() {
        retryButton.setOnClickListener(v -> requireLoggedIn(AuthGate.RETURN_ACTION_CART, this::loadCart));
        checkoutButton.setOnClickListener(v -> checkout());
    }

    private void loadCart() {
        if (busy) {
            return;
        }
        renderState(LoadState.LOADING, "正在加载购物车...");
        loadDetail(
                repository -> repository.cart(),
                this::renderCart,
                (state, message) -> renderState(state, userFacingCommerceError(state, message))
        );
    }

    private void renderCart(CartResponse cart) {
        currentItems.clear();
        if (cart != null && cart.items != null) {
            currentItems.addAll(cart.items);
        }
        selectedItemIds.removeIf(id -> {
            CartResponse.Item item = findItem(id);
            return item == null || !item.isAvailable();
        });
        if (!defaultSelectionApplied) {
            for (CartResponse.Item item : currentItems) {
                if (item != null && item.itemId != null && item.isAvailable()) {
                    selectedItemIds.add(item.itemId);
                }
            }
            defaultSelectionApplied = true;
        }
        adapter.submit(currentItems, selectedItemIds, busy);
        String status = pendingSuccessStatus;
        pendingSuccessStatus = null;
        if (status == null || status.isEmpty()) {
            status = currentItems.isEmpty()
                    ? "购物车为空，请先从商品详情页加入可购买商品。"
                    : "购物车已从后端加载。";
        }
        renderState(currentItems.isEmpty() ? LoadState.EMPTY : LoadState.CONTENT, status);
        updateSummary();
    }

    private void selectItem(CartResponse.Item item, boolean selected) {
        if (item == null || item.itemId == null || !item.isAvailable() || busy) {
            return;
        }
        if (selected) {
            selectedItemIds.add(item.itemId);
        } else {
            selectedItemIds.remove(item.itemId);
        }
        adapter.submit(currentItems, selectedItemIds, busy);
        updateSummary();
    }

    private void updateQuantity(CartResponse.Item item, int delta) {
        if (item == null || item.itemId == null || !item.isAvailable() || busy) {
            return;
        }
        PurchaseFormState.QuantityLimit limit = PurchaseFormState.cartQuantityLimit(
                item.quantityValue(), delta, item.availableStock());
        if (!limit.allowed) {
            if (limit.message != null && !limit.message.isEmpty()) {
                renderState(currentItems.isEmpty() ? LoadState.EMPTY : LoadState.CONTENT, limit.message);
            }
            return;
        }
        setBusy(true, "正在更新数量...");
        loadDetail(
                repository -> repository.updateCartItem(item.itemId, limit.quantity),
                updated -> {
                    setBusy(false, null);
                    loadCart();
                },
                (state, message) -> {
                    setBusy(false, null);
                    renderState(LoadState.ERROR, userFacingCommerceError(state, message));
                }
        );
    }

    private void deleteItem(CartResponse.Item item) {
        if (item == null || item.itemId == null || busy) {
            return;
        }
        setBusy(true, "正在删除购物车商品...");
        loadDetail(
                repository -> {
                    repository.deleteCartItem(item.itemId);
                    return item.itemId;
                },
                deletedId -> {
                    selectedItemIds.remove(deletedId);
                    setBusy(false, null);
                    loadCart();
                },
                (state, message) -> {
                    setBusy(false, null);
                    renderState(LoadState.ERROR, userFacingCommerceError(state, message));
                }
        );
    }

    private void checkout() {
        if (busy) {
            return;
        }
        List<String> itemIds = selectedAvailableItemIds();
        if (itemIds.isEmpty()) {
            renderState(LoadState.CONTENT, "请选择至少一件可购买商品。");
            return;
        }
        AddressSnapshot address = currentAddress();
        if (!PurchaseFormState.validAddress(address)) {
            renderState(LoadState.CONTENT, "请填写收货人、手机号、所在地区和收货地址后再提交订单。");
            return;
        }
        requireLoggedIn(AuthGate.RETURN_ACTION_CART, () -> checkoutAfterLogin(itemIds, address, text(remarkInput)));
    }

    private void checkoutAfterLogin(List<String> itemIds, AddressSnapshot address, String remark) {
        if (busy) {
            return;
        }
        setBusy(true, "正在创建订单...");
        loadDetail(
                repository -> repository.createCartOrder(itemIds, address, remark),
                this::renderCreatedOrder,
                (state, message) -> {
                    setBusy(false, null);
                    renderState(LoadState.ERROR, userFacingCommerceError(state, message));
                }
        );
    }

    private void renderCreatedOrder(Order order) {
        setBusy(false, null);
        String id = order == null ? "未知" : valueOrFallback(order.orderId, "未知");
        String status = order == null ? "已创建" : orderStatus(order.status);
        pendingSuccessStatus = "创建订单成功：" + id + "\n状态：" + status;
        loadCart();
    }

    private List<String> selectedAvailableItemIds() {
        List<String> ids = new ArrayList<>();
        for (CartResponse.Item item : currentItems) {
            if (item != null
                    && item.itemId != null
                    && item.isAvailable()
                    && selectedItemIds.contains(item.itemId)) {
                ids.add(item.itemId);
            }
        }
        return ids;
    }

    private CartResponse.Item findItem(String itemId) {
        for (CartResponse.Item item : currentItems) {
            if (item != null && itemId != null && itemId.equals(item.itemId)) {
                return item;
            }
        }
        return null;
    }

    private void updateSummary() {
        int total = 0;
        int selectedCount = 0;
        for (CartResponse.Item item : currentItems) {
            if (item == null || item.itemId == null || !selectedItemIds.contains(item.itemId) || !item.isAvailable()) {
                continue;
            }
            selectedCount++;
            total += item.rowAmountCent != null ? item.rowAmountCent : item.priceCentValue() * item.quantityValue();
        }
        cartTotal.setText("已选 " + selectedCount + " 件 - " + MoneyFormatter.centsToYuan(total));
        checkoutButton.setEnabled(!busy && selectedCount > 0);
        checkoutButton.setAlpha(checkoutButton.isEnabled() ? 1f : 0.52f);
    }

    private void renderState(LoadState state, String message) {
        boolean loadingVisible = state == LoadState.LOADING;
        boolean hasContent = state == LoadState.CONTENT || (!currentItems.isEmpty() && state == LoadState.ERROR);
        loading.setVisibility(loadingVisible ? View.VISIBLE : View.GONE);
        cartList.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        emptyText.setVisibility(state == LoadState.EMPTY || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        retryButton.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        cartStatus.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
        cartStatus.setText(message == null ? "" : message);
        cartStatus.setTextColor(getColor(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED
                ? R.color.doyu_warn : R.color.doyu_text_muted));
        emptyText.setText(message == null || message.isEmpty()
                ? "购物车为空，后端未返回购物车商品。"
                : message);
        updateSummary();
    }

    private void setBusy(boolean nextBusy, String message) {
        busy = nextBusy;
        adapter.submit(currentItems, selectedItemIds, busy);
        checkoutButton.setEnabled(!busy && !selectedAvailableItemIds().isEmpty());
        retryButton.setEnabled(!busy);
        if (message != null && !message.isEmpty()) {
            renderState(currentItems.isEmpty() ? LoadState.EMPTY : LoadState.CONTENT, message);
        } else {
            updateSummary();
        }
    }

    private boolean requireLoggedIn(String returnAction, Runnable action) {
        pendingAfterLogin = action;
        return AuthGate.runOrRequestLogin(this, loginLauncher, returnAction, () -> {
            pendingAfterLogin = null;
            action.run();
        });
    }

    private AddressSnapshot currentAddress() {
        return new AddressSnapshot(
                text(recipientInput),
                text(phoneInput),
                text(regionInput),
                text(detailInput));
    }

    private static String userFacingCommerceError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return "请先登录后查看购物车或创建订单。";
        }
        if (message == null || message.isEmpty()) {
            return "请求失败，请重试。";
        }
        return PurchaseFormState.commerceMessage(message);
    }

    private static String text(EditText input) {
        return input == null ? "" : input.getText().toString().trim();
    }

    private static int rowAmount(CartResponse.Item item) {
        if (item == null) {
            return 0;
        }
        return item.rowAmountCent != null ? item.rowAmountCent : item.priceCentValue() * item.quantityValue();
    }

    private static int stock(CartResponse.Item item) {
        return item == null ? 0 : Math.max(0, item.availableStock());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {
        interface Listener {
            void onSelectionChanged(CartResponse.Item item, boolean selected);

            void onQuantityDelta(CartResponse.Item item, int delta);

            void onDelete(CartResponse.Item item);
        }

        private final List<CartResponse.Item> items = new ArrayList<>();
        private final Set<String> selectedIds = new HashSet<>();
        private final Listener listener;
        private boolean busy;

        CartAdapter(Listener listener) {
            this.listener = listener;
        }

        void submit(List<CartResponse.Item> nextItems, Set<String> selectedItemIds, boolean nextBusy) {
            items.clear();
            if (nextItems != null) {
                items.addAll(nextItems);
            }
            selectedIds.clear();
            if (selectedItemIds != null) {
                selectedIds.addAll(selectedItemIds);
            }
            busy = nextBusy;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            CartResponse.Item item = items.get(position);
            boolean available = item != null && item.isAvailable();
            boolean selected = item != null && item.itemId != null && selectedIds.contains(item.itemId);
            holder.selected.setOnCheckedChangeListener(null);
            holder.selected.setChecked(selected);
            holder.selected.setEnabled(!busy && available);
            holder.selected.setOnCheckedChangeListener((button, checked) -> {
                if (listener != null) {
                    listener.onSelectionChanged(item, checked);
                }
            });

            holder.title.setText(item == null ? "商品" : item.title());
            holder.spec.setText((item == null ? "默认规格" : item.specName())
                    + " · 库存 " + stock(item)
                    + (available ? "" : " · 不可购买"));
            holder.price.setText(MoneyFormatter.centsToYuan(item == null ? null : item.priceCentValue()));
            holder.quantity.setText(String.valueOf(item == null ? 0 : item.quantityValue()));
            holder.rowAmount.setText(MoneyFormatter.centsToYuan(rowAmount(item)));
            int quantity = item == null ? 0 : item.quantityValue();
            holder.decrease.setEnabled(!busy && available && quantity > 1);
            holder.increase.setEnabled(!busy && available && quantity < stock(item));
            holder.delete.setEnabled(!busy && item != null);
            holder.itemView.setAlpha(available ? 1f : 0.58f);
            holder.decrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityDelta(item, -1);
                }
            });
            holder.increase.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityDelta(item, 1);
                }
            });
            holder.delete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(item);
                }
            });

            String imageUrl = item == null || item.product == null ? "" : item.product.imageUrl;
            if (imageUrl == null || imageUrl.isEmpty()) {
                holder.image.setImageResource(R.drawable.bg_image_placeholder);
            } else {
                Glide.with(holder.image)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final CheckBox selected;
            final ImageView image;
            final TextView title;
            final TextView spec;
            final TextView price;
            final TextView quantity;
            final TextView rowAmount;
            final MaterialButton decrease;
            final MaterialButton increase;
            final MaterialButton delete;

            Holder(@NonNull View itemView) {
                super(itemView);
                selected = itemView.findViewById(R.id.cart_item_selected);
                image = itemView.findViewById(R.id.cart_item_image);
                title = itemView.findViewById(R.id.cart_item_title);
                spec = itemView.findViewById(R.id.cart_item_spec);
                price = itemView.findViewById(R.id.cart_item_price);
                quantity = itemView.findViewById(R.id.cart_item_quantity);
                rowAmount = itemView.findViewById(R.id.cart_item_amount);
                decrease = itemView.findViewById(R.id.cart_item_quantity_decrease);
                increase = itemView.findViewById(R.id.cart_item_quantity_increase);
                delete = itemView.findViewById(R.id.cart_item_delete);
            }
        }
    }

    private static String orderStatus(String status) {
        if ("CREATED".equals(status)) {
            return "已创建";
        }
        if ("CANCELED".equals(status)) {
            return "已取消";
        }
        return valueOrFallback(status, "已创建");
    }
}
