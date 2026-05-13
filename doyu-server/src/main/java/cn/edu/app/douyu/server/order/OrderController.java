package cn.edu.app.douyu.server.order;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Order;
import cn.edu.app.douyu.server.common.Models.OrderItem;
import cn.edu.app.douyu.server.common.Models.Product;
import cn.edu.app.douyu.server.common.Models.Sku;
import cn.edu.app.douyu.server.common.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
        for (OrderSkuRequest item : request.items()) {
            Sku sku = store.skus.get(item.skuId());
            if (sku == null) {
                throw new BizException(ErrorCode.NOT_FOUND, "SKU 不存在");
            }
            Product product = store.products.get(sku.productId());
            if (!"SELF_OPERATED".equals(product.type())) {
                throw new BizException(ErrorCode.CONFLICT, "玩家商品不支持标准订单");
            }
            store.lockStock(sku.id(), item.quantity());
            total += sku.priceCent() * item.quantity();
        }
        Order order = new Order(idGenerator.next("ord"), userId, "SELF_OPERATED", null, "SELF_OPERATED",
                "WAITING_PAYMENT", total, total, request.address(), Instant.now().plusSeconds(900), Instant.now());
        store.orders.put(order.id(), order);
        for (OrderSkuRequest item : request.items()) {
            Sku sku = store.skus.get(item.skuId());
            items.add(new OrderItem(idGenerator.next("oi"), order.id(), sku.id(), sku.productId(), item.quantity(), sku.priceCent()));
        }
        store.orderItems.put(order.id(), items);
        Map<String, Object> response = store.orderView(order);
        store.idempotencyResponses.put(key, objectMapper.writeValueAsString(response));
        return response;
    }

    @GetMapping
    PageResult<Map<String, Object>> orders(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.userOrders(userId).stream().map(store::orderView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @GetMapping("/{orderId}")
    Map<String, Object> order(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        Order order = requireOrder(orderId);
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
        return store.orderView(order);
    }

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

    public record CreateOrderRequest(List<@Valid OrderSkuRequest> items, Map<String, Object> address) {
    }

    public record OrderSkuRequest(@NotBlank String skuId, @Positive int quantity) {
    }
}
