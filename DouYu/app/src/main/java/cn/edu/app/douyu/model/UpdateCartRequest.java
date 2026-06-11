package cn.edu.app.douyu.model;
/**
 * 购物车更新请求 DTO：承载购物车条目的新数量。
 */

public class UpdateCartRequest {
    public int quantity;

    public UpdateCartRequest() {
    }

    public UpdateCartRequest(int quantity) {
        this.quantity = quantity;
    }
}
