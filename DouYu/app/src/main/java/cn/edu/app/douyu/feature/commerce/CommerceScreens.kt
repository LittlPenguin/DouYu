package cn.edu.app.douyu.feature.commerce

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.commerceRepository

@Preview
@Composable
private fun CommerceHomeScreenPreview() { CommerceHomeScreenContent(navController = null) }

@Composable
fun CommerceHomeScreen(navController: NavHostController) { CommerceHomeScreenContent(navController) }

@Composable
private fun CommerceHomeScreenContent(navController: NavHostController?) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("全部商品", "材料包", "独家图纸", "成品手作", "配件工具")
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            DoyuTopBar("商城") {
                IconButton(onClick = { navController?.navigate(AppRoute.CART) }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "购物车", tint = LightPrimary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DoyuSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "搜索手作、图纸或材料包...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 4.dp)
            )

            LazyRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(categories) { index, name ->
                    val selected = index == selectedCategory
                    Surface(
                        onClick = { selectedCategory = index },
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) LightPrimaryContainer else LightSurfaceVariant
                    ) {
                        Text(
                            name,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Content
            val horizontalPadding = adaptiveHorizontalPadding()
            var productsRetryCount by remember { mutableIntStateOf(0) }
            val productsState = safeCallToState(productsRetryCount) { repo.products() }.value
            when (val state = productsState) {
                is UiState.Success -> {
                    val allProducts = state.data.items
                    val filteredProducts = allProducts.filter { product ->
                        val matchesCategory = selectedCategory == 0 || // "全部商品"
                                product.categoryName.contains(categories[selectedCategory])
                        val matchesSearch = searchQuery.isBlank() ||
                                product.title.contains(searchQuery, ignoreCase = true) ||
                                product.description.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }

                    if (filteredProducts.isEmpty()) {
                        EmptyContent(
                            "没有找到商品",
                            "换个关键词试试？",
                            showRetry = false
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Recommended section
                            CommerceSectionHeader(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                                Text("推荐商品", style = MaterialTheme.typography.titleLarge)
                            }

                            // Featured cards
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                filteredProducts.take(2).forEach { product ->
                                    FeaturedProductCard(
                                        product = product,
                                        modifier = Modifier.weight(1f),
                                        onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Product grid
                            CommerceSectionHeader(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                                Text("猜你喜欢", style = MaterialTheme.typography.titleLarge)
                            }

                            // 2-column grid
                            val chunked = filteredProducts.drop(2).chunked(2)
                            chunked.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = horizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    row.forEach { product ->
                                        ProductCard(
                                            product = product,
                                            modifier = Modifier.weight(1f),
                                            onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) }
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
                is UiState.Empty -> {
                    EmptyContent(
                        "暂时没有商品",
                        "商品正在上架中，先去逛逛社区吧。",
                        showRetry = false
                    )
                }
                else -> PageStateView(productsState, onRetry = { productsRetryCount++ })
            }
        }
    }
}

@Composable
private fun CommerceSectionHeader(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.padding(vertical = 8.dp)) {
        content()
    }
}

@Composable
private fun FeaturedProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(product.swatchColor).copy(alpha = 0.3f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = LightSurface.copy(alpha = 0.9f)
                ) {
                    Text(
                        "新品首发",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    product.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    formatPriceCent(product.priceCent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightPrimary
                )
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(product.swatchColor).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                BeadCluster(48.dp)
            }

            // Product info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatPriceCent(product.priceCent),
                        style = MaterialTheme.typography.titleMedium,
                        color = LightPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "库存 ${product.availableStock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── ProductListScreen ──

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
    Scaffold(topBar = { DoyuTopBar("商品列表", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (id, name) ->
                    val isSelected = selectedCategory == id
                    TagChip(
                        name,
                        selected = isSelected,
                        color = if (isSelected) LightPrimaryContainer else LightSurfaceVariant,
                        contentColor = if (isSelected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = id }
                    )
                }
            }
            val productsState = safeCallToState(selectedCategory) {
                if (selectedCategory == null) repo.products() else repo.productsByCategory(selectedCategory!!)
            }.value
            when (val state = productsState) {
                is UiState.Success -> state.data.items.forEach { product ->
                    ProductListItem(product, onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) })
                }
                is UiState.Empty -> EmptyContent("暂时没有商品", "该分类暂无商品，看看其他分类吧。", showRetry = false)
                else -> PageStateView(productsState)
            }
        }
    }
}

@Composable
private fun ProductListItem(product: Product, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(product.swatchColor).copy(alpha = 0.2f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) { BeadCluster(36.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text(product.description, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(formatPriceCent(product.priceCent), color = LightPrimary, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── ProductDetailScreen ──

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
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(cn.edu.app.douyu.core.navigation.AppRoute.LOGIN)
            },
            message = "登录后才能加入购物车"
        )
    }

    LaunchedEffect(addToCartState) {
        if (addToCartState is UiState.Success) {
            addToCartState = null
            navController?.navigate(AppRoute.CART)
        }
    }
    Scaffold(topBar = { DoyuTopBar("商品详情", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = productState) {
                is UiState.Success -> {
                    val product = state.data
                    // Product image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(product.swatchColor).copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center
                    ) { BeadPattern(Modifier.size(120.dp)) }

                    DoyuCard {
                        Text(product.title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(product.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            formatPriceCent(product.priceCent),
                            color = LightPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "库存 ${product.availableStock} · SKU ${product.skus.firstOrNull()?.skuId.orEmpty()}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    DoyuCard {
                        Text("购买说明", style = MaterialTheme.typography.titleMedium)
                        Text("客户端只展示服务端返回金额，不允许编辑最终订单金额。支付结果以服务端订单状态为准。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    val addError = addToCartState as? UiState.Error
                    if (addError != null) {
                        Text(addError.message, color = LightError, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    DoyuPrimaryButton(
                        "加入购物车",
                        onClick = {
                            if (!DoyuAppContainer.isLoggedIn) {
                                showLoginDialog = true
                            } else {
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

// ── CartScreen ──

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
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(cn.edu.app.douyu.core.navigation.AppRoute.LOGIN)
            },
            message = "登录后才能使用购物车"
        )
    }

    // Check login status on entry
    LaunchedEffect(Unit) {
        if (!DoyuAppContainer.isLoggedIn) {
            showLoginDialog = true
        } else {
            cartState = withContext(Dispatchers.IO) {
                runCatching { repo.cart() }
                    .fold(
                        onSuccess = { if (it.items.isEmpty()) UiState.Empty else UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "加载购物车失败") }
                    )
            }
        }
    }

    LaunchedEffect(refreshCount) {
        if (DoyuAppContainer.isLoggedIn && refreshCount > 0) {
            cartState = withContext(Dispatchers.IO) {
                runCatching { repo.cart() }
                    .fold(
                        onSuccess = { if (it.items.isEmpty()) UiState.Empty else UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "加载购物车失败") }
                    )
            }
        }
    }
    Scaffold(topBar = { DoyuTopBar("购物车", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Empty -> EmptyContent("购物车空空如也", "去逛逛商城，挑选心仪的拼豆材料吧。", showRetry = false)
                is UiState.Success -> {
                    val cart = state.data
                    cart.items.forEach { item ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                BeadDot(Color(item.product?.swatchColor ?: 0xFFF6A6B2), size = 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.product?.title ?: "商品", style = MaterialTheme.typography.titleMedium)
                                    Text(item.sku?.specName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (item.quantity > 1) {
                                            cartState = UiState.Loading
                                            cartScope.launch {
                                                cartState = withContext(Dispatchers.IO) {
                                                    runCatching { repo.updateCartItem(item.itemId, item.quantity - 1) }
                                                        .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "操作失败") })
                                                }
                                            }
                                        }
                                    }, enabled = item.quantity > 1) {
                                        Icon(Icons.Filled.Remove, contentDescription = "减少数量")
                                    }
                                    Text("${item.quantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp), textAlign = TextAlign.Center)
                                    IconButton(onClick = {
                                        cartState = UiState.Loading
                                        cartScope.launch {
                                            cartState = withContext(Dispatchers.IO) {
                                                runCatching { repo.updateCartItem(item.itemId, item.quantity + 1) }
                                                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "操作失败") })
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Filled.Add, contentDescription = "增加数量")
                                    }
                                    IconButton(onClick = {
                                        cartState = UiState.Loading
                                        cartScope.launch {
                                            cartState = withContext(Dispatchers.IO) {
                                                runCatching { repo.removeCartItem(item.itemId) }
                                                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "删除失败") })
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = LightError)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    DoyuCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("应付金额", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Text(formatPriceCent(cart.payableAmountCent), color = LightPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    DoyuPrimaryButton("去确认订单", onClick = { navController?.navigate(AppRoute.ORDER_CONFIRM) }, modifier = Modifier.fillMaxWidth())
                }
                else -> PageStateView(cartState)
            }
        }
    }
}

// ── OrderConfirmScreen ──

@Preview
@Composable
private fun OrderConfirmScreenPreview() { OrderConfirmScreenContent(navController = null) }

@Composable
fun OrderConfirmScreen(navController: NavHostController) { OrderConfirmScreenContent(navController) }

@Composable
private fun OrderConfirmScreenContent(navController: NavHostController?) {
    val cartState = safeCallToState { repo.cart() }.value
    Scaffold(topBar = { DoyuTopBar("确认订单", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Success -> {
                    val cart = state.data
                    DisabledFeatureNotice(
                        title = "收货地址暂未接入",
                        message = "当前 UI MVP 不伪装默认地址。地址管理接入前，订单创建和支付单创建保持禁用。"
                    )
                    DoyuCard {
                        Text("订单商品", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        cart.items.forEach {
                            Row(Modifier.fillMaxWidth()) {
                                Text("${it.product?.title ?: "商品"} ${it.sku?.specName ?: ""}", modifier = Modifier.weight(1f))
                                Text("x${it.quantity}")
                            }
                        }
                    }
                    DoyuCard {
                        Text("支付规则", style = MaterialTheme.typography.titleMedium)
                        Text("支付 SDK 当前为占位封装。客户端拉起后展示确认中，最终以服务端订单状态为准。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    DoyuPrimaryButton(
                        "创建支付单 ${formatPriceCent(cart.payableAmountCent)}",
                        onClick = ::disabledClick,
                        icon = Icons.Filled.Payments,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                is UiState.Empty -> EmptyContent("购物车为空", "请先添加商品到购物车。", showRetry = false)
                else -> PageStateView(cartState)
            }
        }
    }
}

// ── PaymentResultScreen ──

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
                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "创建支付单失败") })
                paymentState = result
                paymentId = (result as? UiState.Success)?.data?.paymentId
            } else if (paymentId != null) {
                orderState = runCatching { repo.order(orderId) }
                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "查询订单失败") })
                paymentState = runCatching { repo.paymentStatus(paymentId!!) }
                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "查询支付状态失败") })
            }
        }
    }
    Scaffold(topBar = { DoyuTopBar("支付结果", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = LightPrimary, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(12.dp))
                Text("正在确认支付结果", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("订单 $orderId 已提交查询。支付成功不以 SDK 本地返回为准，需要等待服务端订单或支付单状态确认。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                val orderStatus = (orderState as? UiState.Success)?.data?.status?.name ?: "查询中"
                val payment = (paymentState as? UiState.Success)?.data
                Text("订单状态: $orderStatus · 支付单: ${payment?.paymentId ?: "创建中"} ${payment?.status ?: ""}", color = LightPrimary, fontWeight = FontWeight.SemiBold)
            }
            val currentOrderState = orderState
            val currentPaymentState = paymentState
            if (currentOrderState is UiState.Error) PageStateView(currentOrderState)
            if (currentPaymentState is UiState.Error) PageStateView(currentPaymentState)
            DoyuOutlinedButton("重新查询服务端状态", onClick = { refreshCount++ }, modifier = Modifier.fillMaxWidth())
            DoyuPrimaryButton("返回商城", onClick = { navController?.navigate(cn.edu.app.douyu.core.navigation.BottomTab.COMMERCE.route) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ── MyOrdersScreen ──

@Preview
@Composable
private fun MyOrdersScreenPreview() { MyOrdersScreenContent(navController = null) }

@Composable
fun MyOrdersScreen(navController: NavHostController) { MyOrdersScreenContent(navController) }

@Composable
private fun MyOrdersScreenContent(navController: NavHostController?) {
    val ordersState = safeCallToState { repo.orders() }.value
    Scaffold(topBar = { DoyuTopBar("我的订单", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = ordersState) {
                is UiState.Success -> state.data.items.forEach { order ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "订单 ${order.orderId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TagChip(orderStatusLabel(order.status))
                            }
                            Spacer(Modifier.height(8.dp))
                            order.items.forEach { item ->
                                Text(
                                    "${item.title} x${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                formatPriceCent(order.payableAmountCent),
                                style = MaterialTheme.typography.titleMedium,
                                color = DoyuCoral
                            )
                        }
                    }
                }
                else -> PageStateView(ordersState)
            }
        }
    }
}

private fun orderStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.CREATED -> "已创建"
    OrderStatus.WAITING_PAYMENT -> "待付款"
    OrderStatus.PAID -> "已付款"
    OrderStatus.FULFILLING -> "备货中"
    OrderStatus.SHIPPED -> "已发货"
    OrderStatus.COMPLETED -> "已完成"
    OrderStatus.CANCELED -> "已取消"
    OrderStatus.REFUNDING -> "退款中"
    OrderStatus.REFUNDED -> "已退款"
}
