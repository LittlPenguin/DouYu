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
    public String categoryName;
    public String status;
    public String auditStatus;
    public List<ProductSku> skus;
}
