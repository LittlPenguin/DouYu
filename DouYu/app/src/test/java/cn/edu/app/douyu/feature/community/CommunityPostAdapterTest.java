package cn.edu.app.douyu.feature.community;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommunityPostAdapterTest {
    @Test
    public void masonryHeightUsesCoverRatioAndClamp() {
        assertEquals(168, CommunityPostAdapter.masonryHeightDp(100, 100));
        assertEquals(252, CommunityPostAdapter.masonryHeightDp(200, 300));
        assertEquals(260, CommunityPostAdapter.masonryHeightDp(100, 300));
        assertEquals(120, CommunityPostAdapter.masonryHeightDp(300, 100));
        assertEquals(168, CommunityPostAdapter.masonryHeightDp(null, 100));
        assertEquals(168, CommunityPostAdapter.masonryHeightDp(0, 100));
    }
}
