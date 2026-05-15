package cn.edu.app.douyu.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 响应式布局工具 — 基于屏幕宽度分级。
 * Compact: 手机竖屏 (< 600dp)
 * Medium: 折叠屏/小平板 (600-839dp)
 * Expanded: 平板/桌面 (>= 840dp)
 */
enum class WindowSizeClass { Compact, Medium, Expanded }

@Composable
fun currentWindowSizeClass(): WindowSizeClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WindowSizeClass.Compact
        widthDp < 840 -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

/**
 * 根据窗口宽度自适应水平内边距。
 */
@Composable
fun adaptiveHorizontalPadding(): Dp {
    return when (currentWindowSizeClass()) {
        WindowSizeClass.Compact -> 16.dp
        WindowSizeClass.Medium -> 24.dp
        WindowSizeClass.Expanded -> 48.dp
    }
}

/**
 * 网格列数：Compact=1, Medium=2, Expanded=3
 */
@Composable
fun adaptiveGridColumns(): Int {
    return when (currentWindowSizeClass()) {
        WindowSizeClass.Compact -> 1
        WindowSizeClass.Medium -> 2
        WindowSizeClass.Expanded -> 3
    }
}
