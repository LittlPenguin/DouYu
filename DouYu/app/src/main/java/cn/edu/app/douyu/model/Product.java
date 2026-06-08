package cn.edu.app.douyu.model;

import java.util.List;

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
