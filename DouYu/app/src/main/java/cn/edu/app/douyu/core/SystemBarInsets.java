package cn.edu.app.douyu.core;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.WeakHashMap;

public final class SystemBarInsets {
    private static final WeakHashMap<View, Padding> ORIGINAL_PADDING = new WeakHashMap<>();

    public interface ImeVisibilityListener {
        void onImeVisibilityChanged(boolean visible);
    }

    private SystemBarInsets() {
    }

    public static void applyToContent(Activity activity) {
        View root = contentRoot(activity);
        if (root == null) {
            return;
        }
        Padding padding = originalPadding(root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    padding.left + bars.left,
                    padding.top + bars.top,
                    padding.right + bars.right,
                    padding.bottom + bars.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static void applyToContentWithBottomContainers(Activity activity, View... bottomContainers) {
        applyToContentWithBottomContainers(activity, null, bottomContainers);
    }

    public static void applyToContentWithBottomContainers(
            Activity activity,
            ImeVisibilityListener listener,
            View... bottomContainers
    ) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        View root = contentRoot(activity);
        if (root == null) {
            return;
        }
        Padding rootPadding = originalPadding(root);
        View[] containers = bottomContainers == null ? new View[0] : bottomContainers;
        Padding[] containerPadding = new Padding[containers.length];
        for (int i = 0; i < containers.length; i++) {
            View container = containers[i];
            if (container != null) {
                containerPadding[i] = originalPadding(container);
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            if (listener != null) {
                listener.onImeVisibilityChanged(windowInsets.isVisible(WindowInsetsCompat.Type.ime()));
            }
            view.setPadding(
                    rootPadding.left + bars.left,
                    rootPadding.top + bars.top,
                    rootPadding.right + bars.right,
                    rootPadding.bottom
            );
            int inputBottom = Math.max(ime.bottom, bars.bottom);
            for (int i = 0; i < containers.length; i++) {
                View container = containers[i];
                Padding padding = containerPadding[i];
                if (container != null && padding != null) {
                    container.setPadding(
                            padding.left,
                            padding.top,
                            padding.right,
                            padding.bottom + inputBottom
                    );
                }
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private static View contentRoot(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) content;
        if (group.getChildCount() == 0) {
            return null;
        }
        return group.getChildAt(0);
    }

    private static Padding originalPadding(View view) {
        Padding padding = ORIGINAL_PADDING.get(view);
        if (padding == null) {
            padding = new Padding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
            ORIGINAL_PADDING.put(view, padding);
        }
        return padding;
    }

    private static final class Padding {
        final int left;
        final int top;
        final int right;
        final int bottom;

        Padding(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
