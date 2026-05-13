package cn.edu.app.douyu.server.commerce;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.CartItem;
import cn.edu.app.douyu.server.common.Models.Product;
import cn.edu.app.douyu.server.common.Models.Sku;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CommerceController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;
    private final AuthService authService;

    public CommerceController(InMemoryStore store, IdGenerator idGenerator, AuthService authService) {
        this.store = store;
        this.idGenerator = idGenerator;
        this.authService = authService;
    }

    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.listedProducts().stream().map(store::productView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @GetMapping("/products/{productId}")
    Map<String, Object> product(@PathVariable String productId) {
        Product product = store.products.get(productId);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return store.productView(product);
    }

    @PostMapping("/products")
    Map<String, Object> createPlayerProduct(Authentication authentication, @Valid @RequestBody ProductRequest request) {
        String userId = CurrentUser.userId(authentication);
        User user = authService.requireUser(userId);
        if (("PLAYER_SECOND_HAND".equals(request.type()) || "PLAYER_CUSTOM_SERVICE".equals(request.type()))
                && (user.isMinor() || !"VERIFIED".equals(user.realNameStatus()))) {
            throw new BizException(ErrorCode.FORBIDDEN, "玩家卖家和定制服务发布必须 18+ 实名");
        }
        Instant now = Instant.now();
        Product product = new Product(idGenerator.next("prod"), request.type(), userId, request.title(), request.description(), "player", "DRAFT", "NEED_MANUAL_REVIEW", now);
        store.products.put(product.id(), product);
        Sku sku = new Sku(idGenerator.next("sku"), product.id(), request.sku().specName(), request.sku().priceCent(), request.sku().stock(), 0, "ON_SALE");
        store.skus.put(sku.id(), sku);
        return store.productView(product);
    }

    @GetMapping("/cart")
    Map<String, Object> cart(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.cartItems.values().stream()
                .filter(item -> item.userId().equals(userId))
                .map(item -> Map.of(
                        "itemId", item.id(),
                        "sku", store.skuView(store.skus.get(item.skuId())),
                        "quantity", item.quantity()
                ))
                .toList();
        return Map.of("items", items);
    }

    @PostMapping("/cart/items")
    Map<String, Object> addCart(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        String userId = CurrentUser.userId(authentication);
        Sku sku = requireSku(request.skuId());
        Product product = store.products.get(sku.productId());
        if (!"SELF_OPERATED".equals(product.type())) {
            throw new BizException(ErrorCode.CONFLICT, "玩家商品不支持加入标准购物车");
        }
        String key = userId + ":" + sku.id();
        CartItem existing = store.cartItems.values().stream()
                .filter(item -> (item.userId() + ":" + item.skuId()).equals(key))
                .findFirst()
                .orElse(null);
        CartItem item = existing == null
                ? new CartItem(idGenerator.next("cart"), userId, sku.id(), request.quantity())
                : new CartItem(existing.id(), userId, sku.id(), existing.quantity() + request.quantity());
        store.cartItems.put(item.id(), item);
        return Map.of("itemId", item.id(), "quantity", item.quantity());
    }

    @PatchMapping("/cart/items/{itemId}")
    Map<String, Object> updateCart(Authentication authentication, @PathVariable String itemId, @Valid @RequestBody UpdateCartRequest request) {
        String userId = CurrentUser.userId(authentication);
        CartItem item = requireCartItem(userId, itemId);
        CartItem updated = new CartItem(item.id(), item.userId(), item.skuId(), request.quantity());
        store.cartItems.put(itemId, updated);
        return Map.of("itemId", updated.id(), "quantity", updated.quantity());
    }

    @DeleteMapping("/cart/items/{itemId}")
    Map<String, Object> deleteCart(Authentication authentication, @PathVariable String itemId) {
        String userId = CurrentUser.userId(authentication);
        requireCartItem(userId, itemId);
        store.cartItems.remove(itemId);
        return Map.of("deleted", true);
    }

    private Sku requireSku(String skuId) {
        Sku sku = store.skus.get(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "SKU 不存在");
        }
        return sku;
    }

    private CartItem requireCartItem(String userId, String itemId) {
        CartItem item = store.cartItems.get(itemId);
        if (item == null || !item.userId().equals(userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return item;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record ProductRequest(@NotBlank String type, @NotBlank String title, String description, @Valid ProductSkuRequest sku) {
    }

    public record ProductSkuRequest(@NotBlank String specName, @Positive int priceCent, @Positive int stock) {
    }

    public record CartItemRequest(@NotBlank String skuId, @Positive int quantity) {
    }

    public record UpdateCartRequest(@Positive int quantity) {
    }
}
