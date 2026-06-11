package cn.edu.app.douyu.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public String orderId;
    public String buyerId;
    public String sellerType;
    public String sellerId;
    public String orderType;
    public String status;
    public Integer totalAmountCent;
    public Integer payableAmountCent;
    public AddressSnapshot addressSnapshot;
    public List<Item> items = new ArrayList<>();

    public static class Item {
        public String orderItemId;
        public String skuId;
        public String productId;
        public String title;
        public String specName;
        public Integer quantity;
        public Integer priceCent;
        public Integer rowAmountCent;
    }
}
