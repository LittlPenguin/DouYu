package cn.edu.app.douyu.feature.commerce;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.ui.SummaryItem;

import static org.junit.Assert.assertEquals;

public class CommerceFragmentTest {
    @Test
    public void loadItemsOnlyCreatesCardsForProductsWithRealClickPath() throws Exception {
        PageResponse<Product> page = new PageResponse<>();
        Product missingPath = product("", "Missing path", 1800, "ON_SALE", "SELF_OPERATED");
        Product realProduct = product("prod_100", "Mist beads", 2600, "ON_SALE", "SELF_OPERATED");
        page.items.add(missingPath);
        page.items.add(realProduct);

        List<SummaryItem> items = new CommerceFragment().loadItems(new FakeRepository(page));

        assertEquals(1, items.size());
        assertEquals("prod_100", items.get(0).id);
        assertEquals("Mist beads", items.get(0).title);
        assertEquals("¥26.00 | ON_SALE | SELF_OPERATED", items.get(0).subtitle);
    }

    private static Product product(String productId, String title, Integer priceCents, String status, String type) {
        Product product = new Product();
        product.productId = productId;
        product.title = title;
        product.priceCents = priceCents;
        product.status = status;
        product.type = type;
        return product;
    }

    private static final class FakeRepository extends DoyuRepository {
        private final PageResponse<Product> page;

        FakeRepository(PageResponse<Product> page) {
            super(null);
            this.page = page;
        }

        @Override
        public PageResponse<Product> products() throws IOException {
            return page;
        }
    }
}
