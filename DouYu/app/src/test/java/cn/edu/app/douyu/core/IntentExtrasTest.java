package cn.edu.app.douyu.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntentExtrasTest {
    @Test
    public void preservesMigrationExtraNames() {
        assertEquals("postId", IntentExtras.POST_ID);
        assertEquals("productId", IntentExtras.PRODUCT_ID);
        assertEquals("notificationId", IntentExtras.NOTIFICATION_ID);
        assertEquals("uploadedFileId", IntentExtras.UPLOADED_FILE_ID);
        assertEquals("returnTo", IntentExtras.RETURN_TO);
    }
}
