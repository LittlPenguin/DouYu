package cn.edu.app.douyu.core;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class SystemBarInsets {
    private SystemBarInsets() {
    }

    public static void applyToContent(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof android.view.ViewGroup)) {
            return;
        }
        android.view.ViewGroup group = (android.view.ViewGroup) content;
        if (group.getChildCount() == 0) {
            return;
        }
        View root = group.getChildAt(0);
        int left = root.getPaddingLeft();
        int top = root.getPaddingTop();
        int right = root.getPaddingRight();
        int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(left, top + bars.top, right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
