package cn.edu.app.douyu.network;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoyuApiClientTest {
    @Test
    public void staleTokenRetryIsOnlyEnabledForAnonymousCommunityReads() {
        assertTrue(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/posts/feed"));
        assertTrue(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/topics"));
        assertTrue(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/topics/topic_color/posts"));
        assertTrue(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/posts/post_123"));
        assertTrue(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/posts/post_123/comments"));

        assertFalse(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("POST", "/api/v1/posts/feed"));
        assertFalse(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/users/me"));
        assertFalse(DoyuApiClient.SessionInterceptor.isAnonymousCommunityRead("GET", "/api/v1/notifications"));
    }
}
