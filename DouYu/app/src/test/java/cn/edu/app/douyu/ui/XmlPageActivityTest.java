package cn.edu.app.douyu.ui;

import org.junit.Test;

import cn.edu.app.douyu.network.ApiException;

import static org.junit.Assert.assertEquals;

public class XmlPageActivityTest {
    @Test
    public void detail404UsesMissingResourceBoundaryInsteadOfRawHttpMessage() {
        String message = XmlPageActivity.userFacingDetailError(
                LoadState.ERROR,
                new ApiException(404, "HTTP 404").getMessage()
        );

        assertEquals("内容不存在或已下架。", message);
    }
}
