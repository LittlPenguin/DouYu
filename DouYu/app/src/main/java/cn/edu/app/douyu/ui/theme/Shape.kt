package cn.edu.app.douyu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DoyuShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // ShapeXs: 小按钮、输入框、小标签
    small = RoundedCornerShape(12.dp),       // ShapeSm: 中型按钮、列表项内嵌容器
    medium = RoundedCornerShape(16.dp),      // ShapeMd: 标准按钮、对话框内元素
    large = RoundedCornerShape(20.dp),       // ShapeLg: 卡片、底部弹窗
    extraLarge = RoundedCornerShape(28.dp)   // ShapeXl: 全屏卡片、英雄区块
)
