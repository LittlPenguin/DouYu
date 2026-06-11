package cn.edu.app.douyu.server.commerce;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.CartItemEntity;
import cn.edu.app.douyu.server.common.entity.CartItemRepository;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuEntity;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城接口 Controller：提供商品分类、商品列表、商品详情和购物车接口。
 */
@Tag(name = "Commerce", description = "Products, categories, and cart")
@RestController
@RequestMapping("/api/v1")
public class CommerceController {
    // 商品、SKU 和购物车分别落在不同表，Controller 负责组合客户端需要的视图。
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

    @Operation(summary = "Product list")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/products")
    // 商品列表：只返回上架且审核通过的真实商品，可按分类过滤。
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String categoryId) {
        List<ProductEntity> products = isBlank(categoryId)
                ? productRepository.findByStatusAndAuditStatus("ON_SALE", "PASS")
                : productRepository.findByStatusAndAuditStatusAndCategoryId("ON_SALE", "PASS", categoryId);
        List<Map<String, Object>> items = products.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::productView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "Product categories")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/product-categories")
    // 商品分类：从当前可售商品实时统计分类数量，不依赖前端静态列表。
    Map<String, Object> productCategories() {
        Map<String, CategoryCount> counts = new LinkedHashMap<>();
        for (ProductEntity product : productRepository.findByStatusAndAuditStatus("ON_SALE", "PASS")) {
            String categoryId = valueOrDefault(product.getCategoryId(), "other");
            CategoryCount count = counts.computeIfAbsent(categoryId,
                    id -> new CategoryCount(id, categoryLabel(id, product.getCategoryName()), 0));
            count.count++;
        }
        List<Map<String, Object>> items = counts.values().stream()
                .sorted((a, b) -> Integer.compare(categoryRank(a.categoryId), categoryRank(b.categoryId)))
                .map(c -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("categoryId", c.categoryId);
                    view.put("name", c.name);
                    view.put("productCount", c.count);
                    return view;
                })
                .toList();
        return Map.of("items", items);
    }

    @Operation(summary = "Product detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/products/{productId}")
    // 商品详情：返回商品基础信息和 SKU 列表。
    Map<String, Object> product(@PathVariable String productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Product not found"));
        return productView(product);
    }

    @Operation(summary = "Create player product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/products")
    // 玩家商品创建：当前进入草稿和人工审核状态，未直接上架。
    Map<String, Object> createPlayerProduct(Authentication authentication, @Valid @RequestBody ProductRequest request) {
        String userId = CurrentUser.userId(authentication);
        User user = authService.requireUser(userId);
        if (("PLAYER_SECOND_HAND".equals(request.type()) || "PLAYER_CUSTOM_SERVICE".equals(request.type()))
                && (user.isMinor() || !"VERIFIED".equals(user.realNameStatus()))) {
            throw new BizException(ErrorCode.FORBIDDEN, "Player sellers must be 18+ and verified");
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

    @Operation(summary = "Cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/cart")
    // 购物车：按当前登录用户返回购物车行，并补充商品和 SKU 信息。
    Map<String, Object> cart(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = cartItemRepository.findByUserId(userId).stream()
                .map(this::cartItemView)
                .toList();
        return Map.of("items", items);
    }

    @Operation(summary = "Add cart item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "409", description = "Unavailable product or inventory")
    })
    @PostMapping("/cart/items")
    // 加入购物车：相同 SKU 会累加数量，并校验商品可买和库存充足。
    Map<String, Object> addCart(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        String userId = CurrentUser.userId(authentication);
        SkuEntity sku = requireSku(request.skuId());
        ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
        requirePurchasableSku(sku, product);

        Instant now = Instant.now();
        CartItemEntity existing = cartItemRepository.findByUserIdAndSkuId(userId, sku.getId()).orElse(null);
        CartItemEntity item;
        int targetQuantity = request.quantity();
        if (existing == null) {
            item = new CartItemEntity();
            item.setId(idGenerator.next("cart"));
            item.setUserId(userId);
            item.setSkuId(sku.getId());
            item.setCreatedAt(now);
        } else {
            item = existing;
            targetQuantity = existing.getQuantity() + request.quantity();
        }
        requireQuantityWithinStock(sku, targetQuantity);
        item.setQuantity(targetQuantity);
        item.setUpdatedAt(now);
        cartItemRepository.save(item);
        return Map.of("itemId", item.getId(), "quantity", item.getQuantity());
    }

    @Operation(summary = "Update cart item quantity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PatchMapping("/cart/items/{itemId}")
    // 修改购物车数量：只允许操作自己的购物车行，并重新校验库存。
    Map<String, Object> updateCart(Authentication authentication, @PathVariable String itemId,
                                   @Valid @RequestBody UpdateCartRequest request) {
        String userId = CurrentUser.userId(authentication);
        CartItemEntity item = requireCartItem(userId, itemId);
        SkuEntity sku = requireSku(item.getSkuId());
        ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
        requirePurchasableSku(sku, product);
        requireQuantityWithinStock(sku, request.quantity());

        item.setQuantity(request.quantity());
        item.setUpdatedAt(Instant.now());
        cartItemRepository.save(item);
        return Map.of("itemId", item.getId(), "quantity", item.getQuantity());
    }

    @Operation(summary = "Delete cart item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/cart/items/{itemId}")
    // 删除购物车行：校验归属后直接删除。
    Map<String, Object> deleteCart(Authentication authentication, @PathVariable String itemId) {
        String userId = CurrentUser.userId(authentication);
        requireCartItem(userId, itemId);
        cartItemRepository.deleteById(itemId);
        return Map.of("deleted", true);
    }

    // 购物车行视图：合并 CartItem、SKU、Product，并计算行金额和可购买状态。
    private Map<String, Object> cartItemView(CartItemEntity item) {
        SkuEntity sku = skuRepository.findById(item.getSkuId()).orElse(null);
        ProductEntity product = sku != null ? productRepository.findById(sku.getProductId()).orElse(null) : null;
        boolean available = sku != null
                && isPurchasableSelfOperatedProduct(product)
                && "ON_SALE".equals(sku.getStatus())
                && sku.getAvailableStock() >= item.getQuantity();
        int priceCent = sku != null ? sku.getPriceCent() : 0;

        Map<String, Object> itemData = new LinkedHashMap<>();
        itemData.put("itemId", item.getId());
        itemData.put("skuId", item.getSkuId());
        itemData.put("sku", sku != null ? skuView(sku) : Map.of());
        itemData.put("productId", sku != null ? sku.getProductId() : "");
        itemData.put("product", product != null ? cartProductView(product) : Map.of());
        itemData.put("quantity", item.getQuantity());
        itemData.put("priceCent", priceCent);
        itemData.put("rowAmountCent", priceCent * item.getQuantity());
        itemData.put("available", available);
        return itemData;
    }

    // 校验购物车行属于当前用户，避免越权操作。
    private CartItemEntity requireCartItem(String userId, String itemId) {
        CartItemEntity item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "Cart item not found");
        }
        return item;
    }

    // SKU 是购物车和订单的实际购买单位。
    private SkuEntity requireSku(String skuId) {
        return skuRepository.findById(skuId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "SKU not found"));
    }

    // 标准购物车只支持自营、上架、审核通过且 SKU 在售的商品。
    private void requirePurchasableSku(SkuEntity sku, ProductEntity product) {
        if (product == null || !"ON_SALE".equals(product.getStatus()) || !"PASS".equals(product.getAuditStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "Product is unavailable");
        }
        if (!"SELF_OPERATED".equals(product.getType())) {
            throw new BizException(ErrorCode.CONFLICT, "Player products do not support standard cart");
        }
        if (!"ON_SALE".equals(sku.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "SKU not found");
        }
    }

    // 只读判断，用于购物车列表展示当前行是否仍可购买。
    private boolean isPurchasableSelfOperatedProduct(ProductEntity product) {
        return product != null
                && "SELF_OPERATED".equals(product.getType())
                && "ON_SALE".equals(product.getStatus())
                && "PASS".equals(product.getAuditStatus());
    }

    // 库存校验使用 availableStock，避免用户加入超过可售库存的数量。
    private void requireQuantityWithinStock(SkuEntity sku, int quantity) {
        if (sku.getAvailableStock() < quantity) {
            throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "Inventory is not enough");
        }
    }

    // 商品视图：补充分类名、首个 SKU 价格、总可售库存和 SKU 列表。
    private Map<String, Object> productView(ProductEntity product) {
        List<SkuEntity> productSkus = skuRepository.findByProductId(product.getId());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("productId", product.getId());
        view.put("type", product.getType());
        view.put("sellerId", product.getSellerId());
        view.put("title", product.getTitle());
        view.put("description", product.getDescription());
        view.put("imageUrl", valueOrEmpty(product.getImageUrl()));
        view.put("imageWidth", product.getImageWidth());
        view.put("imageHeight", product.getImageHeight());
        view.put("categoryId", product.getCategoryId());
        view.put("categoryName", categoryLabel(product.getCategoryId(), product.getCategoryName()));
        view.put("status", product.getStatus());
        view.put("auditStatus", product.getAuditStatus());
        view.put("priceCents", firstPrice(productSkus));
        view.put("stock", totalStock(productSkus));
        view.put("skus", productSkus.stream().map(this::skuView).toList());
        view.put("swatchColor", 0xFF6B8E7B);
        return view;
    }

    private Map<String, Object> cartProductView(ProductEntity product) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("productId", product.getId());
        view.put("title", product.getTitle());
        view.put("imageUrl", valueOrEmpty(product.getImageUrl()));
        view.put("type", product.getType());
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

    private String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int firstPrice(List<SkuEntity> skus) {
        if (skus == null || skus.isEmpty()) {
            return 0;
        }
        return skus.get(0).getPriceCent();
    }

    private int totalStock(List<SkuEntity> skus) {
        if (skus == null || skus.isEmpty()) {
            return 0;
        }
        return skus.stream().mapToInt(SkuEntity::getAvailableStock).sum();
    }

    private String categoryLabel(String categoryId, String categoryName) {
        if (!isBlank(categoryName)) {
            return categoryName;
        }
        return switch (valueOrDefault(categoryId, "other")) {
            case "beads" -> "豆子";
            case "boards" -> "板子";
            case "tools" -> "工具";
            case "kits" -> "套装";
            case "player" -> "玩家";
            default -> "精选";
        };
    }

    private int categoryRank(String categoryId) {
        return switch (valueOrDefault(categoryId, "other")) {
            case "beads" -> 0;
            case "boards" -> 1;
            case "tools" -> 2;
            case "kits" -> 3;
            case "player" -> 4;
            default -> 99;
        };
    }

    private static class CategoryCount {
        final String categoryId;
        final String name;
        int count;

        CategoryCount(String categoryId, String name, int count) {
            this.categoryId = categoryId;
            this.name = name;
            this.count = count;
        }
    }

    public record ProductRequest(@NotBlank String type, @NotBlank String title, String description,
                                 @Valid ProductSkuRequest sku) {
    }

    public record ProductSkuRequest(@NotBlank String specName, @Positive int priceCent, @Positive int stock) {
    }

    public record CartItemRequest(@NotBlank String skuId, @Positive int quantity) {
    }

    public record UpdateCartRequest(@Positive int quantity) {
    }
}
