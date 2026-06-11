package cn.edu.app.douyu.model;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    public List<Item> items = new ArrayList<>();

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public static class Item {
        public String itemId;
        public String skuId;
        public String productId;
        public Product product;
        public ProductSku sku;
        public Integer quantity;
        public Integer priceCent;
        public Integer rowAmountCent;
        public Boolean available;

        public int quantityValue() {
            return quantity == null ? 0 : quantity;
        }

        public int priceCentValue() {
            if (priceCent != null) {
                return priceCent;
            }
            return sku == null || sku.priceCent == null ? 0 : sku.priceCent;
        }

        public boolean isAvailable() {
            return Boolean.TRUE.equals(available);
        }

        public String title() {
            return product == null || product.title == null || product.title.isEmpty() ? "商品" : product.title;
        }

        public String specName() {
            return sku == null || sku.specName == null || sku.specName.isEmpty() ? "默认规格" : sku.specName;
        }

        public int availableStock() {
            return sku == null || sku.stock == null ? 0 : Math.max(0, sku.stock);
        }
    }
}
