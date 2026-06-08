package cn.edu.app.douyu.feature.commerce;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommerceProductAdapterTest {
    @Test
    public void masonryHeightFollowsProductImageRatioAndClampsBounds() {
        assertEquals(168, CommerceProductAdapter.masonryHeightDp(100, 100));
        assertEquals(252, CommerceProductAdapter.masonryHeightDp(200, 300));
        assertEquals(260, CommerceProductAdapter.masonryHeightDp(100, 300));
        assertEquals(120, CommerceProductAdapter.masonryHeightDp(300, 100));
        assertEquals(168, CommerceProductAdapter.masonryHeightDp(null, 100));
        assertEquals(168, CommerceProductAdapter.masonryHeightDp(0, 100));
    }
}

