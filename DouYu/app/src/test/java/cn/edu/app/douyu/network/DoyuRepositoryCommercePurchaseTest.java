package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.CartItemRequest;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.CreateOrderRequest;
import cn.edu.app.douyu.model.Order;
import cn.edu.app.douyu.model.UpdateCartRequest;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DoyuRepositoryCommercePurchaseTest {
    @Test
    public void addUpdateAndDeleteCartItemsUseBackendCartApi() throws Exception {
        PurchaseApiState state = new PurchaseApiState();
        DoyuRepository repository = new DoyuRepository(fakeApi(state));

        CartResponse.Item added = repository.addCartItem("sku_1", 2);
        CartResponse.Item updated = repository.updateCartItem("cart_1", 3);
        repository.deleteCartItem("cart_1");
        CartResponse cart = repository.cart();

        assertEquals("sku_1", state.addRequest.skuId);
        assertEquals(2, state.addRequest.quantity);
        assertEquals("cart_1", state.updateItemId);
        assertEquals(3, state.updateRequest.quantity);
        assertEquals("cart_1", state.deletedItemId);
        assertEquals("cart_1", added.itemId);
        assertEquals(Integer.valueOf(3), updated.quantity);
        assertEquals(1, cart.items.size());
    }

    @Test
    public void immediateOrderSendsItemAddressSnapshotAndIdempotencyKey() throws Exception {
        PurchaseApiState state = new PurchaseApiState();
        DoyuRepository repository = new DoyuRepository(fakeApi(state));
        AddressSnapshot address = new AddressSnapshot("Bean Buyer", "13800001111", "Hangzhou", "No. 1 Bean Street");

        Order order = repository.createImmediateOrder("sku_buy", 2, address, "leave at door");

        assertNotNull(state.orderKey);
        assertTrue(state.orderKey.startsWith("android-order-"));
        assertEquals(1, state.orderRequest.items.size());
        assertEquals("sku_buy", state.orderRequest.items.get(0).skuId);
        assertEquals(2, state.orderRequest.items.get(0).quantity);
        assertEquals("Bean Buyer", state.orderRequest.addressSnapshot.recipient);
        assertEquals("leave at door", state.orderRequest.remark);
        assertEquals("CREATED", order.status);
    }

    @Test
    public void cartOrderSendsCartItemIdsAndAddressSnapshot() throws Exception {
        PurchaseApiState state = new PurchaseApiState();
        DoyuRepository repository = new DoyuRepository(fakeApi(state));
        AddressSnapshot address = new AddressSnapshot("Cart Buyer", "13800002222", "Shanghai", "No. 2 Cart Road");

        repository.createCartOrder(List.of("cart_1", "cart_2"), address, "");

        assertEquals(List.of("cart_1", "cart_2"), state.orderRequest.itemIds);
        assertEquals("Cart Buyer", state.orderRequest.addressSnapshot.recipient);
    }

    private static DoyuApi fakeApi(PurchaseApiState state) {
        return (DoyuApi) Proxy.newProxyInstance(
                DoyuApi.class.getClassLoader(),
                new Class<?>[]{DoyuApi.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "FakePurchaseApi";
                    }
                    if ("cart".equals(method.getName())) {
                        CartResponse cart = new CartResponse();
                        CartResponse.Item item = new CartResponse.Item();
                        item.itemId = "cart_1";
                        item.skuId = "sku_1";
                        item.quantity = 3;
                        cart.items.add(item);
                        return new SingleResponseCall<>(Response.success(ok(cart)));
                    }
                    if ("addCartItem".equals(method.getName())) {
                        state.addRequest = (CartItemRequest) args[0];
                        CartResponse.Item item = new CartResponse.Item();
                        item.itemId = "cart_1";
                        item.skuId = state.addRequest.skuId;
                        item.quantity = state.addRequest.quantity;
                        return new SingleResponseCall<>(Response.success(ok(item)));
                    }
                    if ("updateCartItem".equals(method.getName())) {
                        state.updateItemId = (String) args[0];
                        state.updateRequest = (UpdateCartRequest) args[1];
                        CartResponse.Item item = new CartResponse.Item();
                        item.itemId = state.updateItemId;
                        item.quantity = state.updateRequest.quantity;
                        return new SingleResponseCall<>(Response.success(ok(item)));
                    }
                    if ("deleteCartItem".equals(method.getName())) {
                        state.deletedItemId = (String) args[0];
                        return new SingleResponseCall<>(Response.success(ok(java.util.Map.of("deleted", true))));
                    }
                    if ("createOrder".equals(method.getName())) {
                        state.orderKey = (String) args[0];
                        state.orderRequest = (CreateOrderRequest) args[1];
                        Order order = new Order();
                        order.orderId = "ord_1";
                        order.status = "CREATED";
                        return new SingleResponseCall<>(Response.success(ok(order)));
                    }
                    throw new AssertionError("Unexpected API call: " + method);
                });
    }

    private static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = "OK";
        response.data = data;
        return response;
    }

    private static final class PurchaseApiState {
        CartItemRequest addRequest;
        String updateItemId;
        UpdateCartRequest updateRequest;
        String deletedItemId;
        String orderKey;
        CreateOrderRequest orderRequest;
    }
}
