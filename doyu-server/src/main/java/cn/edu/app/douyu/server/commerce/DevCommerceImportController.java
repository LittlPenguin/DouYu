package cn.edu.app.douyu.server.commerce;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuEntity;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开发导入接口：为开发环境导入真实商品数据，避免客户端 mock 商品。
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v1/dev/commerce")
public class DevCommerceImportController {
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;

    public DevCommerceImportController(ProductRepository productRepository, SkuRepository skuRepository) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @PutMapping("/products")
    Map<String, Object> upsertProduct(Authentication authentication, @Valid @RequestBody ProductImportRequest request) {
        CurrentUser.userId(authentication);
        Instant now = Instant.now();
        ProductEntity product = productRepository.findById(request.productId()).orElseGet(ProductEntity::new);
        boolean created = product.getId() == null;
        if (created) {
            product.setId(request.productId());
            product.setCreatedAt(now);
        }
        product.setType(request.type());
        product.setSellerId(null);
        product.setTitle(request.title());
        product.setDescription(request.description() == null ? "" : request.description());
        product.setImageUrl(request.imageUrl());
        product.setImageWidth(request.imageWidth());
        product.setImageHeight(request.imageHeight());
        product.setCategoryId(request.categoryId());
        product.setCategoryName(request.categoryName());
        product.setStatus("ON_SALE");
        product.setAuditStatus("PASS");
        product.setUpdatedAt(now);
        productRepository.save(product);

        SkuEntity sku = skuRepository.findById(request.sku().skuId()).orElseGet(SkuEntity::new);
        if (sku.getId() == null) {
            sku.setId(request.sku().skuId());
            sku.setCreatedAt(now);
        }
        sku.setProductId(product.getId());
        sku.setSpecName(request.sku().specName());
        sku.setPriceCent(request.sku().priceCent());
        sku.setStock(request.sku().stock());
        sku.setLockedStock(0);
        sku.setStatus("ON_SALE");
        sku.setUpdatedAt(now);
        skuRepository.save(sku);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", product.getId());
        response.put("title", product.getTitle());
        response.put("type", product.getType());
        response.put("categoryId", product.getCategoryId());
        response.put("categoryName", product.getCategoryName());
        response.put("imageUrl", product.getImageUrl());
        response.put("imageWidth", product.getImageWidth());
        response.put("imageHeight", product.getImageHeight());
        response.put("status", product.getStatus());
        response.put("auditStatus", product.getAuditStatus());
        response.put("skuId", sku.getId());
        response.put("created", created);
        return response;
    }

    public record ProductImportRequest(@NotBlank String productId,
                                       @NotBlank String type,
                                       @NotBlank String title,
                                       String description,
                                       @NotBlank String imageUrl,
                                       @Min(1) int imageWidth,
                                       @Min(1) int imageHeight,
                                       @NotBlank String categoryId,
                                       @NotBlank String categoryName,
                                       @Valid ProductSkuImportRequest sku) {
    }

    public record ProductSkuImportRequest(@NotBlank String skuId,
                                          @NotBlank String specName,
                                          @Min(1) int priceCent,
                                          @Min(0) int stock) {
    }
}

