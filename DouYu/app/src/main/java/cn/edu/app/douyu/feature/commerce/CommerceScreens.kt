package cn.edu.app.douyu.feature.commerce

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.Cart
import cn.edu.app.douyu.core.model.Order
import cn.edu.app.douyu.core.model.OrderStatus
import cn.edu.app.douyu.core.model.Payment
import cn.edu.app.douyu.core.model.PaymentChannel
import cn.edu.app.douyu.core.model.PaymentStatus
import cn.edu.app.douyu.core.model.Product
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.ui.theme.DoyuSurfaceSoft

private val repo = DoyuAppContainer.commerceRepository


@Preview
@Composable
private fun CommerceHomeScreenPreview() { CommerceHomeScreenContent(navController = null) }

@Composable
fun CommerceHomeScreen(navController: NavHostController) { CommerceHomeScreenContent(navController) }

@Composable
private fun CommerceHomeScreenContent(navController: NavHostController?) {
    Scaffold(
        topBar = {
            DoyuTopBar("商城", action = {
                IconButton(onClick = { navController?.navigate(AppRoute.CART) }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "购物车")
                }
            })
        }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Text("自营精选材料", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "从图纸到材料，优先保证色号清楚、库存可信、购买路径直接。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                DoyuPrimaryButton(
                    "查看商品列表",
                    onClick = { navController?.navigate(AppRoute.PRODUCT_LIST) },
                    icon = Icons.Filled.Storefront,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SectionHeader("推荐商品")
            val productsState = safeCallToState { repo.products() }.value
            when (val state = productsState) {
                is UiState.Success -> state.data.items.take(2).forEach { product ->
                    ProductCard(product, onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) })
                }
                is UiState.Empty -> EmptyContent(
                    "暂时没有商品",
                    "商品正在上架中，先去逛逛社区吧。",
                    showRetry = false
                )
                else -> PageStateView(productsState)
            }
            DoyuCard {
                Text("玩家市场", style = MaterialTheme.typography.titleMedium)
                Text(
                    "二手和定制服务以直连咨询为主，不做平台资金池。卖家需 18+ 实名。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProductListScreenPreview() { ProductListScreenContent(navController = null) }

@Composable
fun ProductListScreen(navController: NavHostController) { ProductListScreenContent(navController) }

@Composable
private fun ProductListScreenContent(navController: NavHostController?) {
    val categories = listOf(
        null to "全部",
        "cat_beginner" to "新手套装",
        "cat_beads" to "豆子",
        "cat_tools" to "工具",
        "cat_palette" to "色卡"
    )
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = {
        DoyuTopBar(
            "商品列表", canGoBack = true, onBack = { navController?.popBackStack() })
    }) { padding ->
        DoyuPage(padding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (id, name) ->
                    val isSelected = selectedCategory == id
                    TagChip(
                        name,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else DoyuSurfaceSoft,
                        modifier = Modifier.clickable { selectedCategory = id }
                    )
                }
            }
            val productsState = safeCallToState(selectedCategory) {
                if (selectedCategory == null) repo.products() else repo.productsByCategory(selectedCategory!!)
            }.value
            when (val state = productsState) {
                is UiState.Success -> state.data.items.forEach { product ->
                    ProductCard(product, onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) })
                }
                is UiState.Empty -> EmptyContent(
                    "暂时没有商品",
                    "该分类暂无商品，看看其他分类吧。",
                    showRetry = false
                )
                else -> PageStateView(productsState)
            }
        }
    }
}

@Preview
@Composable
private fun ProductDetailScreenPreview() { ProductDetailScreenContent(navController = null, productId = "product_001") }

@Composable
fun ProductDetailScreen(navController: NavHostController, productId: String) { ProductDetailScreenContent(navController, productId) }

@Composable
private fun ProductDetailScreenContent(navController: NavHostController?, productId: String) {
    val productState = safeCallToState(productId) { repo.product(productId) }.value
    var addToCartState by remember { mutableStateOf<UiState<Cart>?>(null) }
    val addToCartScope = rememberCoroutineScope()
    LaunchedEffect(addToCartState) {
        if (addToCartState is UiState.Success) {
            addToCartState = null
            navController?.navigate(AppRoute.CART)
        }
    }
    Scaffold(topBar = {
        DoyuTopBar(
            "商品详情", canGoBack = true, onBack = { navController?.popBackStack() })
    }) { padding ->
        DoyuPage(padding) {
            when (val state = productState) {
                is UiState.Success -> {
                    val product = state.data
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
                        Text(
                            formatPriceCent(product.priceCent),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "库存 ${product.availableStock} · SKU ${product.skus.firstOrNull()?.skuId.orEmpty()} · 发货地以后端商品详情为准",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DoyuCard {
                        Text("购买说明", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "客户端只展示服务端返回金额，不允许编辑最终订单金额。支付结果以服务端订单状态为准。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val addError = addToCartState as? UiState.Error
                    if (addError != null) {
                        Text(
                            addError.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    DoyuPrimaryButton(
                        "加入购物车",
                        onClick = {
                            val skuId = product.skus.firstOrNull()?.skuId
                            if (skuId != null) {
                                addToCartScope.launch {
                                    addToCartState = withContext(Dispatchers.IO) {
                                        runCatching { repo.addItemToCart(product.productId, skuId, 1) }
                                            .fold(
                                                onSuccess = { UiState.Success(it) },
                                                onFailure = { UiState.Error(it.message ?: "加入购物车失败") }
                                            )
                                    }
                                }
                            }
                        },
                        icon = Icons.Filled.AddShoppingCart,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(productState)
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
                Text(
                    product.description,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatPriceCent(product.priceCent),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "库存 ${product.availableStock}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "详情")
            }
        }
    }
}

@Preview
@Composable
private fun CartScreenPreview() { CartScreenContent(navController = null) }

@Composable
fun CartScreen(navController: NavHostController) { CartScreenContent(navController) }

@Composable
private fun CartScreenContent(navController: NavHostController?) {
    var cartState by remember { mutableStateOf<UiState<Cart>>(UiState.Loading) }
    var refreshCount by remember { mutableIntStateOf(0) }
    val cartScope = rememberCoroutineScope()
    LaunchedEffect(refreshCount) {
        cartState = withContext(Dispatchers.IO) {
            runCatching { repo.cart() }
                .fold(
                    onSuccess = { if (it.items.isEmpty()) UiState.Empty else UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "加载购物车失败") }
                )
        }
    }
    Scaffold(topBar = {
        DoyuTopBar(
            "购物车", canGoBack = true, onBack = { navController?.popBackStack() })
    }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Empty -> EmptyContent(
                    "购物车空空如也",
                    "去逛逛商城，挑选心仪的拼豆材料吧。",
                    showRetry = false
                )
                is UiState.Success -> {
                    val cart = state.data
                    cart.items.forEach { item ->
                        DoyuCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BeadDot(Color(item.product?.swatchColor ?: 0xFFF6A6B2), size = 32.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.product?.title ?: "商品", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        item.sku?.specName ?: "",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                cartState = UiState.Loading
                                                cartScope.launch {
                                                    cartState = withContext(Dispatchers.IO) {
                                                        runCatching { repo.updateCartItem(item.itemId, item.quantity - 1) }
                                                            .fold(
                                                                onSuccess = { UiState.Success(it) },
                                                                onFailure = { UiState.Error(it.message ?: "操作失败") }
                                                            )
                                                    }
                                                }
                                            }
                                        },
                                        enabled = item.quantity > 1
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "减少数量")
                                    }
                                    Text(
                                        "${item.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.widthIn(min = 24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = {
                                            cartState = UiState.Loading
                                            cartScope.launch {
                                                cartState = withContext(Dispatchers.IO) {
                                                    runCatching { repo.updateCartItem(item.itemId, item.quantity + 1) }
                                                        .fold(
                                                            onSuccess = { UiState.Success(it) },
                                                            onFailure = { UiState.Error(it.message ?: "操作失败") }
                                                        )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "增加数量")
                                    }
                                    IconButton(
                                        onClick = {
                                            cartState = UiState.Loading
                                            cartScope.launch {
                                                cartState = withContext(Dispatchers.IO) {
                                                    runCatching { repo.removeCartItem(item.itemId) }
                                                        .fold(
                                                            onSuccess = { UiState.Success(it) },
                                                            onFailure = { UiState.Error(it.message ?: "删除失败") }
                                                        )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "删除",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                    DoyuCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "应付金额",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                formatPriceCent(cart.payableAmountCent),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    DoyuPrimaryButton(
                        "去确认订单",
                        onClick = { navController?.navigate(AppRoute.ORDER_CONFIRM) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(cartState)
            }
        }
    }
}

@Preview
@Composable
private fun OrderConfirmScreenPreview() { OrderConfirmScreenContent(navController = null) }

@Composable
fun OrderConfirmScreen(navController: NavHostController) { OrderConfirmScreenContent(navController) }

@Composable
private fun OrderConfirmScreenContent(navController: NavHostController?) {
    val cartState = safeCallToState { repo.cart() }.value
    var createState by remember { mutableStateOf<UiState<Order>?>(null) }
    val createScope = rememberCoroutineScope()
    LaunchedEffect(createState) {
        if (createState is UiState.Success) {
            val orderId = (createState as UiState.Success<Order>).data.orderId
            createState = null
            navController?.navigate(AppRoute.paymentResult(orderId))
        }
    }
    Scaffold(topBar = {
        DoyuTopBar(
            "确认订单", canGoBack = true, onBack = { navController?.popBackStack() })
    }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Success -> {
                    val cart = state.data
                    DoyuCard {
                        Text("收货信息", style = MaterialTheme.typography.titleMedium)
                        Text("默认地址（后续接入地址管理）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DoyuCard {
                        Text("订单商品", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        cart.items.forEach {
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    "${it.product?.title ?: "商品"} ${it.sku?.specName ?: ""}",
                                    modifier = Modifier.weight(1f)
                                )
                                Text("x${it.quantity}")
                            }
                        }
                    }
                    DoyuCard {
                        Text("支付规则", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "支付 SDK 当前为占位封装。客户端拉起后展示确认中，最终以服务端订单状态为准。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val createError = createState as? UiState.Error
                    if (createError != null) {
                        Text(
                            createError.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    DoyuPrimaryButton(
                        "创建支付单 ${formatPriceCent(cart.payableAmountCent)}",
                        onClick = {
                            val itemIds = cart.items.mapNotNull { it.itemId.takeIf { _ -> true } }
                            createScope.launch {
                                createState = withContext(Dispatchers.IO) {
                                    runCatching { repo.createOrder(itemIds, "") }
                                        .fold(
                                            onSuccess = { UiState.Success(it) },
                                            onFailure = { UiState.Error(it.message ?: "创建订单失败") }
                                        )
                                }
                            }
                        },
                        icon = Icons.Filled.Payments,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is UiState.Empty -> EmptyContent(
                    "购物车为空",
                    "请先添加商品到购物车。",
                    showRetry = false
                )
                else -> PageStateView(cartState)
            }
        }
    }
}

@Preview
@Composable
private fun PaymentResultScreenPreview() { PaymentResultScreenContent(navController = null, orderId = "order_001") }

@Composable
fun PaymentResultScreen(navController: NavHostController, orderId: String) { PaymentResultScreenContent(navController, orderId) }

@Composable
private fun PaymentResultScreenContent(navController: NavHostController?, orderId: String) {
    var orderState by remember { mutableStateOf<UiState<Order>>(UiState.Loading) }
    var paymentState by remember { mutableStateOf<UiState<Payment>?>(null) }
    var paymentId by remember { mutableStateOf<String?>(null) }
    var refreshCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(refreshCount) {
        withContext(Dispatchers.IO) {
            if (refreshCount == 0) {
                val result = runCatching { repo.createPayment(orderId, PaymentChannel.WECHAT_APP) }
                    .fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "创建支付单失败") }
                    )
                paymentState = result
                paymentId = (result as? UiState.Success)?.data?.paymentId
            } else if (paymentId != null) {
                orderState = runCatching { repo.order(orderId) }
                    .fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "查询订单失败") }
                    )
                paymentState = runCatching { repo.paymentStatus(paymentId!!) }
                    .fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "查询支付状态失败") }
                    )
            }
        }
    }
    Scaffold(topBar = {
        DoyuTopBar(
            "支付结果", canGoBack = true, onBack = { navController?.popBackStack() })
    }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Icon(
                    Icons.Filled.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("正在确认支付结果", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "订单 $orderId 已提交查询。支付成功不以 SDK 本地返回为准，需要等待服务端订单或支付单状态确认。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                val orderStatus = (orderState as? UiState.Success)?.data?.status?.name ?: "查询中"
                val payment = (paymentState as? UiState.Success)?.data
                Text(
                    "订单状态: $orderStatus · 支付单: ${payment?.paymentId ?: "创建中"} ${payment?.status ?: ""}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val currentOrderState = orderState
            val currentPaymentState = paymentState
            if (currentOrderState is UiState.Error) PageStateView(currentOrderState)
            if (currentPaymentState is UiState.Error) PageStateView(currentPaymentState)
            DoyuOutlinedButton(
                "重新查询服务端状态",
                onClick = { refreshCount++ },
                modifier = Modifier.fillMaxWidth()
            )
            DoyuPrimaryButton(
                "返回商城",
                onClick = { navController?.navigate(cn.edu.app.douyu.core.navigation.BottomTab.COMMERCE.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
