package cn.edu.app.douyu.feature.commerce

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.commerceRepository

private data class CommerceCategory(
    val label: String,
    val aliases: Set<String> = emptySet(),
    val types: Set<ProductType> = emptySet()
)

private val commerceCategories = listOf(
    CommerceCategory("全部商品"),
    CommerceCategory("材料包", aliases = setOf("cat_beginner", "cat_beads", "beads", "material", "材料", "新手套装", "豆子")),
    CommerceCategory("独家图纸", aliases = setOf("cat_palette", "pattern", "图纸", "色卡")),
    CommerceCategory("成品手作", aliases = setOf("handmade", "成品", "手作"), types = setOf(ProductType.PLAYER_SECOND_HAND)),
    CommerceCategory("配件工具", aliases = setOf("cat_tools", "tool", "工具", "配件"))
)

private fun Product.matchesCategory(category: CommerceCategory): Boolean {
    if (category.aliases.isEmpty() && category.types.isEmpty()) return true
    return type in category.types ||
            category.aliases.any { alias ->
                categoryId.equals(alias, ignoreCase = true) ||
                        categoryName.contains(alias, ignoreCase = true)
            }
}

private fun Product.matchesSearch(query: String): Boolean {
    val normalized = query.trim()
    return normalized.isBlank() ||
            title.contains(normalized, ignoreCase = true) ||
            description.contains(normalized, ignoreCase = true) ||
            categoryName.contains(normalized, ignoreCase = true)
}

private fun ProductType.label(): String = when (this) {
    ProductType.SELF_OPERATED -> "自营"
    ProductType.PLAYER_SECOND_HAND -> "玩家二手"
    ProductType.PLAYER_CUSTOM_SERVICE -> "定制服务"
}

private fun cartToUiState(cart: Cart): UiState<Cart> =
    if (cart.items.isEmpty()) UiState.Empty else UiState.Success(cart)

@Composable
private fun ProductImageFrame(
    product: Product,
    modifier: Modifier = Modifier,
    beadSize: Dp,
    fallback: @Composable BoxScope.() -> Unit = { BeadCluster(beadSize) }
) {
    CommerceImageFrame(
        imageUrl = product.imageUrl,
        contentDescription = product.title,
        swatchColor = product.swatchColor,
        beadSize = beadSize,
        modifier = modifier,
        fallback = fallback
    )
}

@Composable
private fun CartProductImage(product: CartProductSummary?, modifier: Modifier = Modifier) {
    CommerceImageFrame(
        imageUrl = product?.imageUrl,
        contentDescription = product?.title ?: "商品图片",
        swatchColor = product?.swatchColor ?: 0xFFF6A6B2,
        beadSize = 28.dp,
        modifier = modifier
    )
}

@Composable
private fun CommerceImageFrame(
    imageUrl: String?,
    contentDescription: String,
    swatchColor: Long,
    beadSize: Dp,
    modifier: Modifier = Modifier,
    fallback: @Composable BoxScope.() -> Unit = { BeadCluster(beadSize) }
) {
    val fallbackContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(swatchColor).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            fallback()
        }
    }

    Box(
        modifier = modifier.background(Color(swatchColor).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            fallbackContent()
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { fallbackContent() },
                error = { fallbackContent() }
            )
        }
    }
}

@Preview
@Composable
private fun CommerceHomeScreenPreview() { CommerceHomeScreenContent(navController = null) }

@Composable
fun CommerceHomeScreen(navController: NavHostController) { CommerceHomeScreenContent(navController) }

@Composable
private fun CommerceHomeScreenContent(navController: NavHostController?) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var productsRetryCount by remember { mutableIntStateOf(0) }
    val productsState = safeCallToState(productsRetryCount) { repo.products() }.value

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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(commerceCategories) { index, category ->
                    val selected = index == selectedCategory
                    Surface(
                        onClick = { selectedCategory = index },
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) LightPrimaryContainer else LightSurfaceVariant
                    ) {
                        Text(
                            category.label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            val horizontalPadding = adaptiveHorizontalPadding()
            when (val state = productsState) {
                is UiState.Success -> {
                    val allProducts = state.data.items
                    val category = commerceCategories[selectedCategory]
                    val filteredProducts = allProducts.filter { product ->
                        product.matchesCategory(category) && product.matchesSearch(searchQuery)
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
                            CommerceSectionHeader(
                                title = "今日精选",
                                modifier = Modifier.padding(horizontal = horizontalPadding)
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredProducts.take(4), key = { it.productId }) { product ->
                                    FeaturedProductCard(
                                        product = product,
                                        modifier = Modifier.width(292.dp),
                                        onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            CommerceSectionHeader(
                                title = "猜你喜欢",
                                action = "更多",
                                onAction = { navController?.navigate(AppRoute.PRODUCT_LIST) },
                                modifier = Modifier.padding(horizontal = horizontalPadding)
                            )

                            val chunked = filteredProducts.chunked(2)
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
                            Spacer(Modifier.height(12.dp))
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
private fun CommerceSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, maxLines = 1)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FeaturedProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(166.dp),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProductImageFrame(
                product = product,
                beadSize = 64.dp,
                modifier = Modifier
                    .fillMaxSize()
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
                Spacer(Modifier.height(6.dp))
                Text(
                    product.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${formatPriceCent(product.priceCent)} 起",
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
            ProductImageFrame(
                product = product,
                beadSize = 48.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                TagChip(product.type.label(), selected = product.type == ProductType.SELF_OPERATED)
                Spacer(Modifier.height(8.dp))
                Text(
                    product.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        formatPriceCent(product.priceCent),
                        style = MaterialTheme.typography.titleMedium,
                        color = LightPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (product.type == ProductType.SELF_OPERATED) "库存 ${product.availableStock}" else "直连",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
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
    var selectedCategory by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var productsRetryCount by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { DoyuTopBar("商品列表", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "搜索商品标题、说明或分类..."
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(commerceCategories) { index, category ->
                    val isSelected = selectedCategory == index
                    TagChip(
                        category.label,
                        selected = isSelected,
                        color = if (isSelected) LightPrimaryContainer else LightSurfaceVariant,
                        contentColor = if (isSelected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = index }
                    )
                }
            }
            val productsState = safeCallToState(productsRetryCount) { repo.products() }.value
            when (val state = productsState) {
                is UiState.Success -> {
                    val category = commerceCategories[selectedCategory]
                    val items = state.data.items.filter { it.matchesCategory(category) && it.matchesSearch(searchQuery) }
                    if (items.isEmpty()) {
                        EmptyContent("没有找到商品", "换个分类或关键词再试试。", showRetry = false)
                    } else {
                        items.forEach { product ->
                            ProductListItem(product, onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) })
                        }
                    }
                }
                is UiState.Empty -> EmptyContent("暂时没有商品", "该分类暂无商品，看看其他分类吧。", showRetry = false)
                else -> PageStateView(productsState, onRetry = { productsRetryCount++ })
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
            ProductImageFrame(
                product = product,
                beadSize = 36.dp,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    TagChip(product.type.label(), selected = product.type == ProductType.SELF_OPERATED)
                }
                Text(
                    product.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
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
    var productRetryCount by remember { mutableIntStateOf(0) }
    val productState = safeCallToState(productId, productRetryCount) { repo.product(productId) }.value
    var addToCartState by remember { mutableStateOf<UiState<Cart>?>(null) }
    val addToCartScope = rememberCoroutineScope()
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(AppRoute.login(AppRoute.productDetail(productId)))
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
                    val sku = product.skus.firstOrNull { it.status == SkuStatus.ON_SALE && it.availableStock > 0 }
                    val canUseCart = product.type == ProductType.SELF_OPERATED
                    val canAddToCart = canUseCart &&
                            product.status == ProductStatus.ON_SALE &&
                            product.auditStatus == AuditStatus.PASS &&
                            sku != null
                    ProductImageFrame(
                        product = product,
                        beadSize = 72.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        fallback = { BeadPattern(Modifier.size(120.dp)) }
                    )

                    DoyuCard {
                        TagChip(product.type.label(), selected = canUseCart)
                        Spacer(Modifier.height(10.dp))
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
                            "库存 ${product.availableStock} · ${sku?.specName ?: "暂无可售规格"}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    DoyuCard {
                        Text("购买说明", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (canUseCart) {
                                "自营商品可加入购物车。客户端只展示服务端返回金额，不允许编辑最终订单金额，支付状态以服务端确认为准。"
                            } else {
                                "玩家二手和定制服务当前只展示商品信息，不进入标准购物车；后续走私信咨询或意向单，不与自营商品混单。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    val addError = addToCartState as? UiState.Error
                    if (addError != null) {
                        Text(addError.message, color = LightError, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (!canUseCart) {
                        DisabledFeatureNotice(
                            title = "玩家商品暂不支持标准购物车",
                            message = "当前阶段不伪装玩家交易闭环；请先浏览商品信息，咨询和意向单能力后续单独接入。"
                        )
                    } else if (!canAddToCart) {
                        DisabledFeatureNotice(
                            title = "当前规格不可加购",
                            message = "商品可能未通过审核、已下架或暂无可售库存；请稍后刷新后再试。"
                        )
                    }
                    DoyuPrimaryButton(
                        when {
                            !canUseCart -> "暂不支持加购"
                            addToCartState == UiState.Loading -> "加入中"
                            else -> "加入购物车"
                        },
                        onClick = {
                            if (!DoyuAppContainer.isLoggedIn) {
                                showLoginDialog = true
                            } else if (canAddToCart) {
                                addToCartState = UiState.Loading
                                addToCartScope.launch {
                                    addToCartState = withContext(Dispatchers.IO) {
                                        runCatching { repo.addItemToCart(product.productId, sku.skuId, 1) }
                                            .fold(
                                                onSuccess = { UiState.Success(it) },
                                                onFailure = { UiState.Error(it.message ?: "加入购物车失败") }
                                            )
                                    }
                                }
                            }
                        },
                        icon = Icons.Filled.AddShoppingCart,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canAddToCart && addToCartState != UiState.Loading
                    )
                }
                else -> PageStateView(productState, onRetry = { productRetryCount++ })
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
    var cartActionError by remember { mutableStateOf<String?>(null) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(AppRoute.login(AppRoute.CART))
            },
            message = "登录后才能使用购物车"
        )
    }

    // Check login status on entry
    LaunchedEffect(Unit) {
        if (!DoyuAppContainer.isLoggedIn) {
            showLoginDialog = true
            cartState = UiState.RequireLogin
        } else {
            cartState = withContext(Dispatchers.IO) {
                runCatching { repo.cart() }
                    .fold(onSuccess = ::cartToUiState, onFailure = { UiState.Error(it.message ?: "加载购物车失败") })
            }
        }
    }

    LaunchedEffect(refreshCount) {
        if (DoyuAppContainer.isLoggedIn && refreshCount > 0) {
            cartState = withContext(Dispatchers.IO) {
                runCatching { repo.cart() }
                    .fold(onSuccess = ::cartToUiState, onFailure = { UiState.Error(it.message ?: "加载购物车失败") })
            }
        }
    }
    Scaffold(topBar = { DoyuTopBar("购物车", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Empty -> EmptyContent("购物车空空如也", "去逛逛商城，挑选心仪的拼豆材料吧。", showRetry = false)
                UiState.RequireLogin -> {
                    EmptyContent("登录后查看购物车", "购物车会同步你的自营商品和数量，玩家二手/定制商品不进入标准购物车。", showRetry = false)
                    DoyuPrimaryButton(
                        "去登录",
                        onClick = { navController?.navigate(AppRoute.login(AppRoute.CART)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is UiState.Success -> {
                    val cart = state.data
                    if (cart.items.isEmpty()) {
                        EmptyContent("购物车空空如也", "去逛逛商城，挑选心仪的拼豆材料吧。", showRetry = false)
                        return@DoyuPage
                    }
                    if (cartActionError != null) {
                        Text(cartActionError.orEmpty(), color = LightError, style = MaterialTheme.typography.bodySmall)
                    }
                    cart.items.forEach { item ->
                        val maxStock = item.sku?.availableStock
                        val canIncrease = maxStock == null || item.quantity < maxStock
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CartProductImage(
                                    product = item.product,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.product?.title ?: "商品", style = MaterialTheme.typography.titleMedium)
                                    Text(item.sku?.specName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    if (maxStock != null && item.quantity >= maxStock) {
                                        Text("已达当前库存上限", color = LightError, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (item.quantity > 1) {
                                            cartActionError = null
                                            cartScope.launch {
                                                val result = withContext(Dispatchers.IO) {
                                                    runCatching { repo.updateCartItem(item.itemId, item.quantity - 1) }
                                                }
                                                result.fold(
                                                    onSuccess = { cartState = cartToUiState(it) },
                                                    onFailure = { cartActionError = it.message ?: "数量调整失败" }
                                                )
                                            }
                                        }
                                    }, enabled = item.quantity > 1) {
                                        Icon(Icons.Filled.Remove, contentDescription = "减少数量")
                                    }
                                    Text("${item.quantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp), textAlign = TextAlign.Center)
                                    IconButton(onClick = {
                                        cartActionError = null
                                        cartScope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                runCatching { repo.updateCartItem(item.itemId, item.quantity + 1) }
                                            }
                                            result.fold(
                                                onSuccess = { cartState = cartToUiState(it) },
                                                onFailure = { cartActionError = it.message ?: "数量调整失败" }
                                            )
                                        }
                                    }, enabled = canIncrease) {
                                        Icon(Icons.Filled.Add, contentDescription = "增加数量")
                                    }
                                    IconButton(onClick = {
                                        cartActionError = null
                                        cartScope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                runCatching { repo.removeCartItem(item.itemId) }
                                            }
                                            result.fold(
                                                onSuccess = { cartState = cartToUiState(it) },
                                                onFailure = { cartActionError = it.message ?: "删除失败" }
                                            )
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
                else -> PageStateView(cartState, onRetry = { refreshCount++ })
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
    var cartRetryCount by remember { mutableIntStateOf(0) }
    val cartState = if (DoyuAppContainer.isLoggedIn) {
        safeCallToState(cartRetryCount) { repo.cart() }.value
    } else {
        UiState.RequireLogin
    }
    Scaffold(topBar = { DoyuTopBar("确认订单", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = cartState) {
                is UiState.Success -> {
                    val cart = state.data
                    if (cart.items.isEmpty()) {
                        EmptyContent("购物车为空", "请先添加自营商品到购物车，再进入订单确认。", showRetry = false)
                        return@DoyuPage
                    }
                    DisabledFeatureNotice(
                        title = "收货地址暂未接入",
                        message = "当前 UI MVP 不伪装默认地址。地址管理接入前不会创建订单，也不会创建支付单。"
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
                        Text("当前不拉起微信/支付宝，也不展示渠道完成态。仅展示联调支付单和服务端确认状态。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    DoyuPrimaryButton(
                        "暂不能下单 · ${formatPriceCent(cart.payableAmountCent)}",
                        onClick = ::disabledClick,
                        icon = Icons.Filled.Payments,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                is UiState.Empty -> EmptyContent("购物车为空", "请先添加商品到购物车。", showRetry = false)
                UiState.RequireLogin -> {
                    EmptyContent("登录后确认订单", "登录后才能读取购物车、确认收货信息并创建订单。", showRetry = false)
                    DoyuPrimaryButton(
                        "去登录",
                        onClick = { navController?.navigate(AppRoute.login(AppRoute.ORDER_CONFIRM)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(cartState, onRetry = { cartRetryCount++ })
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
    val paymentScope = rememberCoroutineScope()
    LaunchedEffect(refreshCount) {
        orderState = withContext(Dispatchers.IO) {
            runCatching { repo.order(orderId) }
                .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "查询订单失败") })
        }
        val currentPaymentId = paymentId
        if (currentPaymentId != null) {
            paymentState = withContext(Dispatchers.IO) {
                runCatching { repo.paymentStatus(currentPaymentId) }
                    .fold(onSuccess = { UiState.Success(it) }, onFailure = { UiState.Error(it.message ?: "查询支付状态失败") })
            }
        }
    }
    Scaffold(topBar = { DoyuTopBar("支付结果", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = LightPrimary, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(12.dp))
                Text("联调支付状态", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("订单 $orderId 只查询服务端状态；当前不拉起真实微信/支付宝支付，支付单需要显式创建。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                val orderStatus = (orderState as? UiState.Success)?.data?.status?.name ?: "查询中"
                val payment = (paymentState as? UiState.Success)?.data
                Text("订单状态: $orderStatus · 支付单: ${payment?.paymentId ?: "未创建"} ${payment?.status ?: ""}", color = LightPrimary, fontWeight = FontWeight.SemiBold)
                if (payment != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "渠道: ${payment.channel.name} · 金额: ${formatPriceCent(payment.amountCent)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            val currentOrderState = orderState
            val currentPaymentState = paymentState
            if (currentOrderState is UiState.Error) PageStateView(currentOrderState, onRetry = { refreshCount++ })
            if (currentPaymentState is UiState.Error) {
                PageStateView(
                    currentPaymentState,
                    onRetry = {
                        if (paymentId != null) refreshCount++ else paymentState = null
                    }
                )
            }
            DoyuOutlinedButton("重新查询服务端状态", onClick = { refreshCount++ }, modifier = Modifier.fillMaxWidth())
            DoyuPrimaryButton(
                when {
                    paymentState == UiState.Loading -> "创建中"
                    paymentId != null -> "已创建联调支付单"
                    else -> "创建联调支付单"
                },
                onClick = {
                    paymentState = UiState.Loading
                    paymentScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { repo.createPayment(orderId, PaymentChannel.WECHAT_APP) }
                        }
                        result.fold(
                            onSuccess = {
                                paymentState = UiState.Success(it)
                                paymentId = it.paymentId
                            },
                            onFailure = { paymentState = UiState.Error(it.message ?: "创建联调支付单失败") }
                        )
                    }
                },
                icon = Icons.Filled.Payments,
                modifier = Modifier.fillMaxWidth(),
                enabled = currentOrderState is UiState.Success && paymentState != UiState.Loading && paymentId == null
            )
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
    var ordersRetryCount by remember { mutableIntStateOf(0) }
    val ordersState = if (DoyuAppContainer.isLoggedIn) {
        safeCallToState(ordersRetryCount) { repo.orders() }.value
    } else {
        UiState.RequireLogin
    }
    Scaffold(topBar = { DoyuTopBar("我的订单", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = ordersState) {
                is UiState.Success -> {
                    if (state.data.items.isEmpty()) {
                        EmptyContent("还没有订单", "去商城看看材料包，订单会在服务端创建后显示在这里。", showRetry = false)
                    } else {
                        state.data.items.forEach { order ->
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
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        TagChip(orderStatusLabel(order.status))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    order.items.forEach { item ->
                                        Text(
                                            "${item.title} x${item.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        formatPriceCent(order.payableAmountCent),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DoyuCoral
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    DoyuOutlinedButton(
                                        "查看联调支付状态",
                                        onClick = { navController?.navigate(AppRoute.paymentResult(order.orderId)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
                UiState.RequireLogin -> {
                    EmptyContent("登录后查看订单", "订单列表只展示服务端已创建的订单和联调支付状态。", showRetry = false)
                    DoyuPrimaryButton(
                        "去登录",
                        onClick = { navController?.navigate(AppRoute.login(AppRoute.MY_ORDERS)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(ordersState, onRetry = { ordersRetryCount++ })
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
