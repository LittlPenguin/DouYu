package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 创建订单请求 DTO：承载立即购买或购物车下单的商品和地址快照。
 */

public class CreateOrderRequest {
    public List<String> itemIds;
    public List<OrderLineRequest> items;
    public AddressSnapshot addressSnapshot;
    public String remark;

    public CreateOrderRequest() {
    }

    public static CreateOrderRequest immediate(String skuId, int quantity, AddressSnapshot addressSnapshot, String remark) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.items = List.of(new OrderLineRequest(skuId, quantity));
        request.addressSnapshot = addressSnapshot;
        request.remark = remark;
        return request;
    }

    public static CreateOrderRequest fromCart(List<String> itemIds, AddressSnapshot addressSnapshot, String remark) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.itemIds = itemIds;
        request.addressSnapshot = addressSnapshot;
        request.remark = remark;
        return request;
    }

    public static class OrderLineRequest {
        public String skuId;
        public int quantity;

        public OrderLineRequest() {
        }

        public OrderLineRequest(String skuId, int quantity) {
            this.skuId = skuId;
            this.quantity = quantity;
        }
    }
}
