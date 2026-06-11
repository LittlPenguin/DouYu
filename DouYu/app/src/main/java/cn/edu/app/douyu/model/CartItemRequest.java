package cn.edu.app.douyu.model;

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
