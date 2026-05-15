package cn.edu.app.douyu.server.order;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.CartItem;
import cn.edu.app.douyu.server.common.Models.Order;
import cn.edu.app.douyu.server.common.Models.OrderItem;
import cn.edu.app.douyu.server.common.Models.Product;
import cn.edu.app.douyu.server.common.Models.Sku;
import cn.edu.app.douyu.server.common.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "订单", description = "创建订单、订单列表、取消订单")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public OrderController(InMemoryStore store, IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.store = store;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建订单", description = "创建标准订单，需要 Idempotency-Key 头")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "SKU 不存在"),
            @ApiResponse(responseCode = "409", description = "库存不足或玩家商品不支持标准订单")
    })
    @PostMapping
    Map<String, Object> create(Authentication authentication,
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody CreateOrderRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        String key = "ORDER:" + userId + ":" + idempotencyKey;
        if (store.idempotencyResponses.containsKey(key)) {
            return readMap(store.idempotencyResponses.get(key));
        }
        int total = 0;
        List<OrderItem> items = new ArrayList<>();
        List<CartItem> resolvedItems = new ArrayList<>();
        for (String itemId : request.itemIds()) {
            CartItem cartItem = store.cartItems.get(itemId);
            if (cartItem == null || !cartItem.userId().equals(userId)) {
                throw new BizException(ErrorCode.NOT_FOUND, "购物车项不存在");
            }
            Sku sku = store.skus.get(cartItem.skuId());
            if (sku == null) {
                throw new BizException(ErrorCode.NOT_FOUND, "SKU 不存在");
            }
            Product product = store.products.get(sku.productId());
            if (!"SELF_OPERATED".equals(product.type())) {
                throw new BizException(ErrorCode.CONFLICT, "玩家商品不支持标准订单");
            }
            store.lockStock(sku.id(), cartItem.quantity());
            total += sku.priceCent() * cartItem.quantity();
            resolvedItems.add(cartItem);
        }
        Map<String, Object> address = Map.of("addressId", request.addressId());
        Order order = new Order(idGenerator.next("ord"), userId, "SELF_OPERATED", null, "SELF_OPERATED",
                "WAITING_PAYMENT", total, total, address, Instant.now().plusSeconds(900), Instant.now());
        store.orders.put(order.id(), order);
        for (CartItem cartItem : resolvedItems) {
            Sku sku = store.skus.get(cartItem.skuId());
            items.add(new OrderItem(idGenerator.next("oi"), order.id(), sku.id(), sku.productId(), cartItem.quantity(), sku.priceCent()));
            store.cartItems.remove(cartItem.id());
        }
        store.orderItems.put(order.id(), items);
        Map<String, Object> response = store.orderView(order);
        store.idempotencyResponses.put(key, objectMapper.writeValueAsString(response));
        return response;
    }

    @Operation(summary = "订单列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping
    PageResult<Map<String, Object>> orders(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.userOrders(userId).stream().map(store::orderView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权查看该订单"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @GetMapping("/{orderId}")
    Map<String, Object> order(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        Order order = requireOrder(orderId);
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
        return store.orderView(order);
    }

    @Operation(summary = "取消订单")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权取消该订单"),
            @ApiResponse(responseCode = "404", description = "订单不存在"),
            @ApiResponse(responseCode = "409", description = "当前订单状态不可取消")
    })
    @PostMapping("/{orderId}/cancel")
    Map<String, Object> cancel(Authentication authentication, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        Order order = requireOrder(orderId);
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权取消该订单");
        }
        if (!"WAITING_PAYMENT".equals(order.status()) && !"CREATED".equals(order.status())) {
            throw new BizException(ErrorCode.CONFLICT, "当前订单状态不可取消");
        }
        Order canceled = new Order(order.id(), order.buyerId(), order.sellerType(), order.sellerId(), order.orderType(),
                "CANCELED", order.totalAmountCent(), order.payableAmountCent(), order.address(), order.expiresAt(), order.createdAt());
        store.orders.put(orderId, canceled);
        return store.orderView(canceled);
    }

    private Order requireOrder(String orderId) {
        Order order = store.orders.get(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record CreateOrderRequest(List<@NotBlank String> itemIds, @NotBlank String addressId, String remark) {
    }
}
