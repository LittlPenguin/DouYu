package cn.edu.app.douyu.server.commerce;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.*;
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
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final CartItemRepository cartItemRepository;
    private final IdGenerator idGenerator;
    private final AuthService authService;

    public CommerceController(ProductRepository productRepository, SkuRepository skuRepository,
                              CartItemRepository cartItemRepository, IdGenerator idGenerator, AuthService authService) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.cartItemRepository = cartItemRepository;
        this.idGenerator = idGenerator;
        this.authService = authService;
    }

    @Operation(summary = "商品列表")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = productRepository.findByStatusNot("DELETED").stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::productView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "商品详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @GetMapping("/products/{productId}")
    Map<String, Object> product(@PathVariable String productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "商品不存在"));
        return productView(product);
    }

    @Operation(summary = "发布玩家商品")
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
        ProductEntity product = new ProductEntity();
        product.setId(idGenerator.next("prod"));
        product.setType(request.type());
        product.setSellerId(userId);
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setCategoryId("player");
        product.setStatus("DRAFT");
        product.setAuditStatus("NEED_MANUAL_REVIEW");
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        productRepository.save(product);
        SkuEntity sku = new SkuEntity();
        sku.setId(idGenerator.next("sku"));
        sku.setProductId(product.getId());
        sku.setSpecName(request.sku().specName());
        sku.setPriceCent(request.sku().priceCent());
        sku.setStock(request.sku().stock());
        sku.setLockedStock(0);
        sku.setStatus("ON_SALE");
        sku.setCreatedAt(now);
        sku.setUpdatedAt(now);
        skuRepository.save(sku);
        return productView(product);
    }

    @Operation(summary = "购物车")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/cart")
    Map<String, Object> cart(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = cartItemRepository.findByUserId(userId).stream().map(item -> {
            SkuEntity sku = skuRepository.findById(item.getSkuId()).orElse(null);
            ProductEntity product = sku != null ? productRepository.findById(sku.getProductId()).orElse(null) : null;
            Map<String, Object> itemData = new java.util.LinkedHashMap<>();
            itemData.put("itemId", item.getId());
            itemData.put("skuId", item.getSkuId());
            itemData.put("sku", sku != null ? skuView(sku) : Map.of());
            itemData.put("productId", sku != null ? sku.getProductId() : "");
            itemData.put("product", product != null ? cartProductView(product) : Map.of());
            itemData.put("quantity", item.getQuantity());
            return itemData;
        }).toList();
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
        SkuEntity sku = skuRepository.findById(request.skuId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "SKU 不存在"));
        ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
        if (product != null && !"SELF_OPERATED".equals(product.getType())) {
            throw new BizException(ErrorCode.CONFLICT, "玩家商品不支持加入标准购物车");
        }
        Instant now = Instant.now();
        CartItemEntity existing = cartItemRepository.findByUserIdAndSkuId(userId, sku.getId()).orElse(null);
        CartItemEntity item;
        if (existing == null) {
            item = new CartItemEntity();
            item.setId(idGenerator.next("cart"));
            item.setUserId(userId);
            item.setSkuId(sku.getId());
            item.setQuantity(request.quantity());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
        } else {
            item = existing;
            item.setQuantity(existing.getQuantity() + request.quantity());
            item.setUpdatedAt(now);
        }
        cartItemRepository.save(item);
        return Map.of("itemId", item.getId(), "quantity", item.getQuantity());
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
        CartItemEntity item = requireCartItem(userId, itemId);
        item.setQuantity(request.quantity());
        item.setUpdatedAt(Instant.now());
        cartItemRepository.save(item);
        return Map.of("itemId", item.getId(), "quantity", item.getQuantity());
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
        cartItemRepository.deleteById(itemId);
        return Map.of("deleted", true);
    }

    private CartItemEntity requireCartItem(String userId, String itemId) {
        CartItemEntity item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return item;
    }

    private Map<String, Object> productView(ProductEntity product) {
        List<SkuEntity> productSkus = skuRepository.findByProductId(product.getId());
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("productId", product.getId());
        view.put("type", product.getType());
        view.put("sellerId", product.getSellerId());
        view.put("title", product.getTitle());
        view.put("description", product.getDescription());
        view.put("imageUrl", valueOrEmpty(product.getImageUrl()));
        view.put("categoryId", product.getCategoryId());
        view.put("categoryName", product.getCategoryId());
        view.put("status", product.getStatus());
        view.put("auditStatus", product.getAuditStatus());
        view.put("skus", productSkus.stream().map(this::skuView).toList());
        view.put("swatchColor", 0xFF6B8E7B);
        return view;
    }

    private Map<String, Object> cartProductView(ProductEntity product) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("title", product.getTitle());
        view.put("imageUrl", valueOrEmpty(product.getImageUrl()));
        return view;
    }

    private Map<String, Object> skuView(SkuEntity sku) {
        return Map.of(
                "skuId", sku.getId(),
                "productId", sku.getProductId(),
                "specName", sku.getSpecName(),
                "priceCent", sku.getPriceCent(),
                "stock", sku.getAvailableStock(),
                "status", sku.getStatus()
        );
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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
