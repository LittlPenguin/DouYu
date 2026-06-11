package cn.edu.app.douyu.feature.commerce;

import org.junit.Test;

import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductSku;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PurchaseFormStateTest {
    @Test
    public void selectedSkuRequiresSelfOperatedOnSaleProductAndSkuStock() {
        Product product = new Product();
        product.type = "SELF_OPERATED";
        product.status = "ON_SALE";
        product.auditStatus = "PASS";
        ProductSku sku = new ProductSku();
        sku.skuId = "sku_1";
        sku.status = "ON_SALE";
        sku.stock = 4;
        product.skus = List.of(sku);

        assertEquals("sku_1", PurchaseFormState.firstPurchasableSku(product).skuId);

        product.type = "PLAYER_SECOND_HAND";
        assertNull(PurchaseFormState.firstPurchasableSku(product));
    }

    @Test
    public void quantityClampsBetweenOneAndAvailableStock() {
        assertEquals(1, PurchaseFormState.clampQuantity(0, 5));
        assertEquals(3, PurchaseFormState.clampQuantity(3, 5));
        assertEquals(5, PurchaseFormState.clampQuantity(8, 5));
        assertEquals(0, PurchaseFormState.clampQuantity(1, 0));
    }

    @Test
    public void addressSnapshotRequiresManualRecipientPhoneRegionAndDetail() {
        assertFalse(PurchaseFormState.validAddress(new AddressSnapshot("", "13800001111", "Hangzhou", "No. 1")));
        assertFalse(PurchaseFormState.validAddress(new AddressSnapshot("Buyer", "", "Hangzhou", "No. 1")));
        assertFalse(PurchaseFormState.validAddress(new AddressSnapshot("Buyer", "13800001111", "", "No. 1")));
        assertFalse(PurchaseFormState.validAddress(new AddressSnapshot("Buyer", "13800001111", "Hangzhou", "")));
        assertTrue(PurchaseFormState.validAddress(new AddressSnapshot("Buyer", "13800001111", "Hangzhou", "No. 1")));
    }

    @Test
    public void addToCartRejectsQuantityThatWouldPushExistingSkuOverStock() {
        ProductSku sku = new ProductSku();
        sku.skuId = "sku_12";
        sku.stock = 12;
        CartResponse cart = new CartResponse();
        CartResponse.Item existing = new CartResponse.Item();
        existing.skuId = "sku_12";
        existing.quantity = 5;
        cart.items.add(existing);

        PurchaseFormState.CartLimit limit = PurchaseFormState.addToCartLimit(sku, cart, 12);

        assertFalse(limit.allowed);
        assertEquals(12, limit.stock);
        assertEquals(5, limit.existingQuantity);
        assertEquals(7, limit.remainingQuantity);
        assertEquals(7, limit.allowedQuantity);
        assertEquals("库存仅剩 12 件，购物车中已有 5 件，最多还能加入 7 件", limit.message);
    }

    @Test
    public void cartQuantityIncreaseAtStockLimitIsRejectedWithFriendlyMessage() {
        PurchaseFormState.QuantityLimit limit = PurchaseFormState.cartQuantityLimit(12, 1, 12);

        assertFalse(limit.allowed);
        assertEquals(12, limit.quantity);
        assertEquals("已达到库存上限", limit.message);
    }

    @Test
    public void commerceConflictErrorsMapToChineseInventoryMessage() {
        assertEquals("库存不足，请减少数量后重试。",
                PurchaseFormState.commerceMessage("HTTP 409"));
        assertEquals("库存不足，请减少数量后重试。",
                PurchaseFormState.commerceMessage("Inventory is not enough"));
        assertEquals("请求状态已变化，请刷新后重试。",
                PurchaseFormState.commerceMessage("CONFLICT"));
        assertEquals("请求状态已变化，请刷新后重试。",
                PurchaseFormState.commerceMessage("请求失败：CONFLICT。请返回后重试。"));
    }
}
