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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "商城", description = "商品列表、购物车、玩家商品发布")
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

    @Operation(summary = "商品列表", description = "获取已上架商品列表（公开接口）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.listedProducts().stream().map(store::productView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "商品详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @GetMapping("/products/{productId}")
    Map<String, Object> product(@PathVariable String productId) {
        Product product = store.products.get(productId);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return store.productView(product);
    }

    @Operation(summary = "发布玩家商品", description = "发布玩家二手或定制商品，要求 18+ 实名")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "未成年或未实名")
    })
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

    @Operation(summary = "购物车")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/cart")
    Map<String, Object> cart(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.cartItems.values().stream()
                .filter(item -> item.userId().equals(userId))
                .map(item -> {
                    Sku sku = store.skus.get(item.skuId());
                    Product product = sku != null ? store.products.get(sku.productId()) : null;
                    Map<String, Object> itemData = new java.util.LinkedHashMap<>();
                    itemData.put("itemId", item.id());
                    itemData.put("skuId", item.skuId());
                    itemData.put("sku", store.skuView(sku));
                    itemData.put("productId", sku != null ? sku.productId() : "");
                    itemData.put("product", product != null ? Map.of("title", product.title(), "imageUrl", "") : Map.of());
                    itemData.put("quantity", item.quantity());
                    return itemData;
                })
                .toList();
        return Map.of("items", items);
    }

    @Operation(summary = "加入购物车")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "SKU 不存在"),
            @ApiResponse(responseCode = "409", description = "玩家商品不支持加入标准购物车")
    })
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

    @Operation(summary = "修改购物车数量")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "购物车项不存在")
    })
    @PatchMapping("/cart/items/{itemId}")
    Map<String, Object> updateCart(Authentication authentication, @PathVariable String itemId, @Valid @RequestBody UpdateCartRequest request) {
        String userId = CurrentUser.userId(authentication);
        CartItem item = requireCartItem(userId, itemId);
        CartItem updated = new CartItem(item.id(), item.userId(), item.skuId(), request.quantity());
        store.cartItems.put(itemId, updated);
        return Map.of("itemId", updated.id(), "quantity", updated.quantity());
    }

    @Operation(summary = "移除购物车商品")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "购物车项不存在")
    })
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
