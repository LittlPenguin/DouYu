package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 商品响应 DTO：承载商城商品、图片、分类和 SKU 列表。
 */

public class Product {
    public String productId;
    public String title;
    public String name;
    public String description;
    public String imageUrl;
    public Integer priceCents;
    public String type;
    public String productType;
    public String categoryId;
    public String categoryName;
    public Integer imageWidth;
    public Integer imageHeight;
    public Integer stock;
    public String status;
    public String auditStatus;
    public List<ProductSku> skus;
}
