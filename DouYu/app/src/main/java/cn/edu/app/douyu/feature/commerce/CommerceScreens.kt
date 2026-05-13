package cn.edu.app.douyu.feature.commerce

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.MockCommerceRepository
import cn.edu.app.douyu.core.model.Product
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*

private val repo = MockCommerceRepository()

@Composable
fun CommerceHomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            DoyuTopBar("商城", action = {
                IconButton(onClick = { navController.navigate(AppRoute.CART) }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "购物车")
                }
            })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Text("自营精选材料", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text("从图纸到材料，优先保证色号清楚、库存可信、购买路径直接。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                DoyuPrimaryButton("查看商品列表", onClick = { navController.navigate(AppRoute.PRODUCT_LIST) }, icon = Icons.Filled.Storefront, modifier = Modifier.fillMaxWidth())
            }
            SectionHeader("推荐商品")
            repo.products().items.take(2).forEach { product ->
                ProductCard(product, onClick = { navController.navigate(AppRoute.productDetail(product.id)) })
            }
            DoyuCard {
                Text("玩家市场", style = MaterialTheme.typography.titleMedium)
                Text("二手和定制服务以直连咨询为主，不做平台资金池。卖家需 18+ 实名。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ProductListScreen(navController: NavHostController) {
    Scaffold(topBar = { DoyuTopBar("商品列表", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagChip("新手套装")
                TagChip("豆子")
                TagChip("工具")
                TagChip("色卡")
            }
            repo.products().items.forEach { product ->
                ProductCard(product, onClick = { navController.navigate(AppRoute.productDetail(product.id)) })
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    DoyuCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color(product.swatchColor), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                BeadCluster(42.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text(product.description, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatPriceCent(product.priceCent), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("库存 ${product.stock}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "详情")
            }
        }
    }
}

@Composable
fun ProductDetailScreen(navController: NavHostController, productId: String) {
    val product = repo.product(productId)
    Scaffold(topBar = { DoyuTopBar("商品详情", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Color(product.swatchColor), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                BeadPattern(Modifier.size(120.dp))
            }
            DoyuCard {
                Text(product.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(product.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(formatPriceCent(product.priceCent), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                Text("库存 ${product.stock} · 发货地以后端商品详情为准", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuCard {
                Text("购买说明", style = MaterialTheme.typography.titleMedium)
                Text("客户端只展示服务端返回金额，不允许编辑最终订单金额。支付结果以服务端订单状态为准。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuPrimaryButton("加入购物车", onClick = { navController.navigate(AppRoute.CART) }, icon = Icons.Filled.AddShoppingCart, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CartScreen(navController: NavHostController) {
    val cart = repo.cart()
    Scaffold(topBar = { DoyuTopBar("购物车", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            cart.items.forEach {
                DoyuCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BeadDot(Color(it.product.swatchColor), size = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(it.product.title, style = MaterialTheme.typography.titleMedium)
                            Text("数量 x${it.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatPriceCent(it.product.priceCent * it.quantity), fontWeight = FontWeight.Bold)
                    }
                }
            }
            DoyuCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("应付金额", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(formatPriceCent(cart.payableAmountCent), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                }
            }
            DoyuPrimaryButton("去确认订单", onClick = { navController.navigate(AppRoute.ORDER_CONFIRM) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun OrderConfirmScreen(navController: NavHostController) {
    val order = repo.order()
    Scaffold(topBar = { DoyuTopBar("确认订单", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Text("收货信息", style = MaterialTheme.typography.titleMedium)
                Text(order.addressSnapshot, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuCard {
                Text("订单商品", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                order.items.forEach {
                    Row(Modifier.fillMaxWidth()) {
                        Text(it.product.title, modifier = Modifier.weight(1f))
                        Text("x${it.quantity}")
                    }
                }
            }
            DoyuCard {
                Text("支付规则", style = MaterialTheme.typography.titleMedium)
                Text("支付 SDK 当前为占位封装。客户端拉起后展示确认中，最终以服务端订单状态为准。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuPrimaryButton("创建支付单 ${formatPriceCent(order.payableAmountCent)}", onClick = { navController.navigate(AppRoute.paymentResult(order.id)) }, icon = Icons.Filled.Payments, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PaymentResultScreen(navController: NavHostController, orderId: String) {
    Scaffold(topBar = { DoyuTopBar("支付结果", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(12.dp))
                Text("正在确认支付结果", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("订单 $orderId 已提交查询。支付成功不以 SDK 本地返回为准，需要等待服务端订单状态确认。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuOutlinedButton("查看订单详情占位", onClick = {}, modifier = Modifier.fillMaxWidth())
            DoyuPrimaryButton("返回商城", onClick = { navController.navigate(cn.edu.app.douyu.core.navigation.BottomTab.COMMERCE.route) }, modifier = Modifier.fillMaxWidth())
        }
    }
}
