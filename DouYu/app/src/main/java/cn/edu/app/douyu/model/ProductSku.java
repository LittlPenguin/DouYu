package cn.edu.app.douyu.model;
/**
 * 商品 SKU DTO：承载规格、价格、库存和销售状态。
 */

public class ProductSku {
    public String skuId;
    public String productId;
    public String specName;
    public Integer priceCent;
    public Integer stock;
    public String status;
}
