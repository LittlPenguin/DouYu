package cn.edu.app.douyu.model;
/**
 * 购物车请求 DTO：承载添加购物车时的 SKU 和数量。
 */

public class CartItemRequest {
    public String skuId;
    public int quantity;

    public CartItemRequest() {
    }

    public CartItemRequest(String skuId, int quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }
}
