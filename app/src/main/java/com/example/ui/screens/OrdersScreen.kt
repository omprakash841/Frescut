package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderDomainModel
import com.example.ui.components.ObserveSmartBottomNavScroll
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutCreamRatingBg
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutGreenBg
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutGreenStep
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutPrimaryButton
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutSelectedPinkBg
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutThinDivider
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.StatusAmberBar
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusAmberText
import com.example.ui.theme.StatusBlueBar
import com.example.ui.theme.StatusBlueBg
import com.example.ui.theme.StatusBlueText
import com.example.ui.theme.StatusGreenBar
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenText
import com.example.ui.theme.StatusGreyBar
import com.example.ui.theme.StatusGreyBg
import com.example.ui.theme.StatusGreyText
import com.example.ui.theme.StatusRedBar
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusRedText
import java.text.NumberFormat
import java.util.Locale

private val orderFilterTabs = listOf(
    "All",
    "Order Placed",
    "Processing",
    "Packed",
    "Out for Delivery",
    "Delivered",
    "Cancelled"
)
private val orderStepLabels = listOf(
    "Order\nPlaced",
    "Processing",
    "Packed",
    "Out for\nDelivery",
    "Delivered"
)

private data class OrderStatusStyle(
    val displayLabel: String,
    val backgroundColor: Color,
    val textColor: Color,
    val accentBarColor: Color,
    val isActive: Boolean,
    val stepIndex: Int
)

private fun resolveOrderStatusStyle(rawStatus: String): OrderStatusStyle {
    val trimmed = rawStatus.trim()
    return when (trimmed.lowercase(Locale.ROOT)) {
        "order placed" -> OrderStatusStyle(
            displayLabel = "Order Placed",
            backgroundColor = StatusAmberBg,
            textColor = StatusAmberText,
            accentBarColor = StatusAmberBar,
            isActive = true,
            stepIndex = 0
        )
        "processing" -> OrderStatusStyle(
            displayLabel = "Processing",
            backgroundColor = StatusBlueBg,
            textColor = StatusBlueText,
            accentBarColor = StatusBlueBar,
            isActive = true,
            stepIndex = 1
        )
        "packed" -> OrderStatusStyle(
            displayLabel = "Packed",
            backgroundColor = StatusBlueBg,
            textColor = StatusBlueText,
            accentBarColor = StatusBlueBar,
            isActive = true,
            stepIndex = 2
        )
        "out for delivery" -> OrderStatusStyle(
            displayLabel = "Out for Delivery",
            backgroundColor = StatusRedBg,
            textColor = StatusRedText,
            accentBarColor = StatusRedBar,
            isActive = true,
            stepIndex = 3
        )
        "delivered" -> OrderStatusStyle(
            displayLabel = "Delivered",
            backgroundColor = StatusGreenBg,
            textColor = StatusGreenText,
            accentBarColor = StatusGreenBar,
            isActive = false,
            stepIndex = 4
        )
        "cancelled", "canceled" -> OrderStatusStyle(
            displayLabel = "Cancelled",
            backgroundColor = StatusGreyBg,
            textColor = StatusGreyText,
            accentBarColor = StatusGreyBar,
            isActive = false,
            stepIndex = 4
        )
        else -> OrderStatusStyle(
            displayLabel = trimmed.ifEmpty { "Unknown" },
            backgroundColor = StatusGreyBg,
            textColor = StatusGreyText,
            accentBarColor = StatusGreyBar,
            isActive = false,
            stepIndex = 0
        )
    }
}

private fun formatInr(amount: Int): String {
    return "₹" + NumberFormat.getNumberInstance(Locale.US).format(amount)
}

@Composable
fun OrdersScreen(
    orders: List<OrderDomainModel>,
    selectedStatusFilter: String,
    selectedOrderIdForDetail: String?,
    cartCount: Int,
    onSelectStatusFilter: (String) -> Unit,
    onOpenOrderDetail: (String) -> Unit,
    onBackFromOrderDetail: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onReorderClick: (OrderDomainModel) -> Unit,
    onTrackLiveClick: (OrderDomainModel) -> Unit,
    onBrowseProductsClick: () -> Unit = {},
    onRateOrder: (String, Int) -> Unit = { _, _ -> },
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedOrder = remember(orders, selectedOrderIdForDetail) {
        orders.find { it.orderId == selectedOrderIdForDetail }
    }

    if (selectedOrder != null) {
        BackHandler(onBack = onBackFromOrderDetail)
        OrderDetailsView(
            order = selectedOrder,
            cartCount = cartCount,
            onBackClick = onBackFromOrderDetail,
            onSearchClick = onSearchClick,
            onCartClick = onCartClick,
            onTrackLiveClick = { onTrackLiveClick(selectedOrder) },
            onReorderClick = { onReorderClick(selectedOrder) },
            onRateOrder = { stars -> onRateOrder(selectedOrder.orderId, stars) },
            onBottomNavVisibilityChanged = onBottomNavVisibilityChanged,
            modifier = modifier
        )
    } else {
        var isOrderSearchVisible by remember { mutableStateOf(false) }
        var orderSearchQuery by remember { mutableStateOf("") }
        var isOrderSearchFocused by remember { mutableStateOf(false) }

        val listState = rememberLazyListState()
        ObserveSmartBottomNavScroll(
            listState = listState,
            onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
        )
        val filteredOrders = remember(orders, selectedStatusFilter, orderSearchQuery) {
            val statusFiltered = if (selectedStatusFilter.trim().equals("All", ignoreCase = true)) {
                orders
            } else {
                orders.filter {
                    it.status.trim().equals(selectedStatusFilter.trim(), ignoreCase = true)
                }
            }
            val query = orderSearchQuery.trim()
            if (query.isEmpty()) {
                statusFiltered
            } else {
                val normalizedQuery = query.removePrefix("#")
                statusFiltered.filter { order ->
                    order.orderId.contains(query, ignoreCase = true) ||
                        order.orderId.removePrefix("#").contains(normalizedQuery, ignoreCase = true)
                }
            }
        }

        val activeOrders = remember(filteredOrders) {
            filteredOrders
                .filter { resolveOrderStatusStyle(it.status).isActive }
                .sortedByDescending { it.createdAtMillis }
        }

        val pastOrders = remember(filteredOrders) {
            filteredOrders
                .filter { !resolveOrderStatusStyle(it.status).isActive }
                .sortedByDescending { it.createdAtMillis }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(FrescutWhite)
                .windowInsetsPadding(WindowInsets.statusBars)
                .testTag("my_orders_screen")
        ) {
            // Top Bar: "My Orders" + Search (toggles order number search) + Cart with Badge
            OrdersTopAppBar(
                title = "My Orders",
                showBackButton = false,
                cartCount = cartCount,
                onBackClick = {},
                onSearchClick = {
                    isOrderSearchVisible = !isOrderSearchVisible
                    if (!isOrderSearchVisible) {
                        orderSearchQuery = ""
                    }
                },
                onCartClick = onCartClick
            )

            if (isOrderSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .height(42.dp)
                        .clip(FrescutOutlineShape)
                        .background(FrescutWhite)
                        .border(
                            width = 1.dp,
                            color = if (isOrderSearchFocused) FrescutFocusRed else FrescutBorderLight,
                            shape = FrescutOutlineShape
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = if (isOrderSearchFocused) FrescutFocusRed else FrescutTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (orderSearchQuery.isEmpty()) {
                            Text(
                                text = "Search by order ID (e.g. #FC-151125-0001)",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = orderSearchQuery,
                            onValueChange = { orderSearchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = FrescutTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isOrderSearchFocused = it.isFocused }
                                .testTag("orders_search_input")
                        )
                    }
                    if (orderSearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { orderSearchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear order search",
                                tint = FrescutTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Order Status Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderFilterTabs.forEach { tab ->
                    val isSelected = selectedStatusFilter.trim().equals(tab, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(FrescutOutlineShape)
                            .background(if (isSelected) FrescutSelectedPinkBg else FrescutWhite)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FrescutFocusRed else FrescutBorderLight,
                                shape = FrescutOutlineShape
                            )
                            .clickable { onSelectStatusFilter(tab) }
                            .testTag("order_filter_${tab.lowercase().replace(" ", "_")}")
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) FrescutFocusRed else FrescutTextPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }

            FrescutThinDivider()

            // Orders List (or Empty State when no orders exist in Firestore for this user)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(FrescutWhite)
                    .testTag("orders_lazy_column"),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 12.dp,
                    bottom = 148.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredOrders.isEmpty()) {
                    item(key = "empty_orders_state") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 36.dp)
                                .testTag("empty_orders_card"),
                            shape = FrescutOutlineShape,
                            colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                                    .padding(horizontal = 24.dp, vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(FrescutRedLightBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = null,
                                        tint = FrescutRedBright,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No orders yet",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("empty_orders_title")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Explore fresh cuts and place your first order.",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                FrescutPrimaryButton(
                                    text = "Browse products",
                                    onClick = onBrowseProductsClick,
                                    modifier = Modifier.testTag("browse_products_btn")
                                )
                            }
                        }
                    }
                } else {
                    if (activeOrders.isNotEmpty()) {
                        item(key = "active_orders_header") {
                            Text(
                                text = "Active orders",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier
                                    .padding(top = 2.dp, bottom = 2.dp)
                                    .testTag("active_orders_heading")
                            )
                        }
                        items(
                            items = activeOrders,
                            key = { "active_${it.orderId}_${it.status.trim()}" }
                        ) { order ->
                            OrderSummaryCard(
                                order = order,
                                onCardClick = { onOpenOrderDetail(order.orderId) }
                            )
                        }
                    }

                    if (pastOrders.isNotEmpty()) {
                        item(key = "past_orders_header") {
                            Text(
                                text = "Past orders",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier
                                    .padding(
                                        top = if (activeOrders.isNotEmpty()) 6.dp else 2.dp,
                                        bottom = 2.dp
                                    )
                                    .testTag("past_orders_heading")
                            )
                        }
                        items(
                            items = pastOrders,
                            key = { "past_${it.orderId}_${it.status.trim()}" }
                        ) { order ->
                            OrderSummaryCard(
                                order = order,
                                onCardClick = { onOpenOrderDetail(order.orderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersTopAppBar(
    title: String,
    showBackButton: Boolean,
    cartCount: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("order_details_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FrescutTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (showBackButton) 20.sp else 24.sp
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("orders_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Orders & Products",
                    tint = FrescutTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCartClick)
                    .testTag("orders_cart_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Shopping Cart",
                    tint = FrescutTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                if (cartCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(FrescutRedBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartCount.toString(),
                            color = FrescutWhite,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    order: OrderDomainModel,
    onCardClick: () -> Unit
) {
    val statusStyle = remember(order.status) { resolveOrderStatusStyle(order.status) }
    val itemNamesLine = remember(order.items) {
        order.items.joinToString(", ") { item ->
            if (item.quantity > 1) "${item.name} x${item.quantity}" else item.name
        }.ifBlank { "Order items" }
    }
    val extraItemCount = (order.items.size - 3).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FrescutOutlineShape)
            .clickable(onClick = onCardClick)
            .testTag("order_card_${order.orderId}"),
        shape = FrescutOutlineShape,
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FrescutOutlineShape)
                .border(
                    width = 1.dp,
                    color = FrescutBorderLight,
                    shape = FrescutOutlineShape
                )
                .drawWithContent {
                    drawContent()
                    if (statusStyle.isActive) {
                        val barWidthPx = 3.dp.toPx()
                        drawRect(
                            color = statusStyle.accentBarColor,
                            topLeft = Offset.Zero,
                            size = Size(width = barWidthPx, height = size.height)
                        )
                    }
                }
                .padding(12.dp)
        ) {
            // Part 1 (Top): Order number (bold) and date/time (grey) on Left, Status chip on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.orderId,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (order.placedAtText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = order.placedAtText,
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OrderStatusPillBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            FrescutThinDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Part 2 (Middle): Up to 3 small item photos (+N if > 3) and item names on 1 line with ellipsis
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.items.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        order.items.take(3).forEach { item ->
                            Image(
                                painter = painterResource(id = item.imageRes),
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        FrescutBorderLight,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (extraItemCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FrescutSurface)
                                    .border(
                                        1.dp,
                                        FrescutBorderLight,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extraItemCount",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Text(
                    text = itemNamesLine,
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            FrescutThinDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Part 3 (Bottom): Total amount on Left, "View details" on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatInr(order.totalAmount),
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("view_details_btn_${order.orderId}")
                ) {
                    Text(
                        text = "View details",
                        color = FrescutFocusRed,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View details",
                        tint = FrescutFocusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatusPillBadge(status: String) {
    val style = remember(status) { resolveOrderStatusStyle(status) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(style.backgroundColor)
            .padding(horizontal = 9.dp, vertical = 4.dp)
            .testTag("order_status_chip_${style.displayLabel.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = style.displayLabel,
            color = style.textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun OrderStepTimeline(
    activeStepIndex: Int,
    timestamps: List<String>,
    showTimestamps: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Horizontal Stepper Circles and Connecting Lines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Connecting background and active progress lines
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                val totalWidth = size.width
                val stepSpan = totalWidth / 4f

                // Grey full track
                drawLine(
                    color = Color(0xFFD1D5DB),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(totalWidth, size.height / 2f),
                    strokeWidth = 5f
                )

                // Green active track up to activeStepIndex
                val activeEnd = (activeStepIndex.coerceIn(0, 4)) * stepSpan
                if (activeEnd > 0f) {
                    drawLine(
                        color = Color(0xFF16A34A),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(activeEnd, size.height / 2f),
                        strokeWidth = 5f
                    )
                }
            }

            // 5 Step Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0..4) {
                    when {
                        index < activeStepIndex -> {
                            // Completed Step: Solid green circle with white checkmark
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(FrescutGreenStep),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completed",
                                    tint = FrescutWhite,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        index == activeStepIndex -> {
                            // Current Active Step: Double green ring with solid center dot
                            Canvas(modifier = Modifier.size(22.dp)) {
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 2f
                                )
                                drawCircle(
                                    color = Color(0xFF16A34A),
                                    radius = size.minDimension / 2f - 2f,
                                    style = Stroke(width = 5f)
                                )
                                drawCircle(
                                    color = Color(0xFF16A34A),
                                    radius = size.minDimension / 4.5f
                                )
                            }
                        }
                        else -> {
                            // Upcoming Step: White circle with grey border
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(FrescutWhite)
                                    .border(2.dp, Color(0xFFD1D5DB), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Step Labels and Optional Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            orderStepLabels.forEachIndexed { index, label ->
                val isReached = index <= activeStepIndex
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        color = if (isReached) FrescutTextPrimary else FrescutTextSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isReached) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (showTimestamps) {
                        val timeText = timestamps.getOrNull(index).orEmpty()
                        if (timeText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = timeText,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsView(
    order: OrderDomainModel,
    cartCount: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onTrackLiveClick: () -> Unit,
    onReorderClick: () -> Unit,
    onRateOrder: (Int) -> Unit = {},
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val detailListState = rememberLazyListState()
    ObserveSmartBottomNavScroll(
        listState = detailListState,
        onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
    )

    val activeStepIndex = resolveOrderStatusStyle(order.status).stepIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("order_details_screen")
    ) {
        // Top App Bar: "<- Order Details" + Search + Cart Badge
        OrdersTopAppBar(
            title = "Order Details",
            showBackButton = true,
            cartCount = cartCount,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onCartClick = onCartClick
        )

        LazyColumn(
            state = detailListState,
            modifier = Modifier
                .fillMaxSize()
                .background(FrescutWhite),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 6.dp,
                bottom = 148.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Order Status & Timeline Card
            item(key = "order_header_timeline_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = order.orderId,
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                )
                                if (order.placedAtText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Placed ${order.placedAtText}",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            OrderStatusPillBadge(status = order.status)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5-Step Progress Timeline with Timestamps (only real Firestore entries displayed)
                        OrderStepTimeline(
                            activeStepIndex = activeStepIndex,
                            timestamps = order.timelineTimes,
                            showTimestamps = true
                        )

                        // Live Tracking / ETA section shown ONLY when real tracking fields exist in Firestore
                        if (order.hasLiveTrackingData) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FrescutGreenBg)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalShipping,
                                            contentDescription = null,
                                            tint = FrescutGreenDiscount,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            if (!order.etaText.isNullOrBlank()) {
                                                Text(
                                                    text = order.etaText,
                                                    color = FrescutGreenDiscount,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.5.sp
                                                    )
                                                )
                                            }
                                            if (!order.deliveryPartner.isNullOrBlank()) {
                                                Text(
                                                    text = "Delivery Partner: ${order.deliveryPartner}",
                                                    color = FrescutTextPrimary,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                            if (!order.chillerTemp.isNullOrBlank()) {
                                                Text(
                                                    text = "Chiller Temp: ${order.chillerTemp}",
                                                    color = FrescutGreenDiscount,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            if (!order.route.isNullOrBlank()) {
                                                Text(
                                                    text = "Route: ${order.route}",
                                                    color = FrescutTextSecondary,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .border(1.dp, FrescutRedBright, RoundedCornerShape(50))
                                            .clickable(onClick = onTrackLiveClick)
                                            .testTag("track_live_btn"),
                                        shape = RoundedCornerShape(50),
                                        color = FrescutWhite
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.LocationOn,
                                                contentDescription = null,
                                                tint = FrescutRedBright,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Track Info",
                                                color = FrescutRedBright,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Order Items Card ("Order Items (3)")
            item(key = "order_items_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Order Items (${order.totalItemsCount})",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        order.items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        painter = painterResource(id = item.imageRes),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .width(68.dp)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.name,
                                            color = FrescutTextPrimary,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.weight,
                                            color = FrescutTextSecondary,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${item.price * item.quantity}",
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Qty: ${item.quantity}",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            if (index < order.items.lastIndex) {
                                FrescutThinDivider()
                            }
                        }
                    }
                }
            }

            // 3. Order Summary Card ("Item Total", "Delivery Fee", "Packaging Charge", "Total Paid")
            item(key = "order_summary_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Order Summary",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryRowItem(
                            label = "Item Total",
                            value = formatInr(order.itemTotal)
                        )
                        if (order.deliveryFee > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRowItem(
                                label = "Delivery Fee",
                                value = formatInr(order.deliveryFee)
                            )
                        }
                        if (order.packagingCharge > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRowItem(
                                label = "Packaging Charge",
                                value = formatInr(order.packagingCharge)
                            )
                        }

                        if (order.paymentMethod.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRowItem(
                                label = "Payment Method",
                                value = order.paymentMethod
                            )
                        }

                        val singleLineAddress = remember(order.houseNumber, order.floor, order.landmark, order.address) {
                            val parts = listOf(order.houseNumber, order.floor, order.landmark)
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            if (parts.isNotEmpty()) {
                                parts.joinToString(", ")
                            } else {
                                order.address
                                    .replace(Regex(",?\\s*Phone:\\s*[^,]+", RegexOption.IGNORE_CASE), "")
                                    .trim()
                            }
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current
                        if (singleLineAddress.isNotBlank() || order.receiverPhone.isNotBlank() || (order.latitude != null && order.longitude != null)) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FrescutThinDivider()
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Delivery Address",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    if (order.latitude != null && order.longitude != null) {
                                        Row(
                                            modifier = Modifier
                                                .clip(FrescutOutlineShape)
                                                .background(FrescutSelectedPinkBg)
                                                .border(1.dp, FrescutFocusRed, FrescutOutlineShape)
                                                .clickable {
                                                    val lat = order.latitude
                                                    val lng = order.longitude
                                                    val navIntent = android.content.Intent(
                                                        android.content.Intent.ACTION_VIEW,
                                                        android.net.Uri.parse("google.navigation:q=$lat,$lng")
                                                    ).apply {
                                                        setPackage("com.google.android.apps.maps")
                                                    }
                                                    val fallbackIntent = android.content.Intent(
                                                        android.content.Intent.ACTION_VIEW,
                                                        android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                                                    )
                                                    try {
                                                        context.startActivity(navIntent)
                                                    } catch (e: Exception) {
                                                        runCatching { context.startActivity(fallbackIntent) }
                                                    }
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                                .testTag("order_detail_navigate_btn"),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.LocationOn,
                                                contentDescription = "Navigate",
                                                tint = FrescutFocusRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Navigate",
                                                color = FrescutFocusRed,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                                if (singleLineAddress.isNotBlank()) {
                                    Text(
                                        text = singleLineAddress,
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("order_detail_address_line")
                                    )
                                }
                                if (order.receiverPhone.isNotBlank()) {
                                    Text(
                                        text = "Receiver: ${order.receiverPhone}",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("order_detail_receiver_line")
                                    )
                                }
                            }
                        }
                        if (order.deliveryInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRowItem(
                                label = "Instructions",
                                value = order.deliveryInstructions
                            )
                        }
                        if (order.rating != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRowItem(
                                label = "Your Rating",
                                value = "${order.rating} ★"
                            )
                        } else if (order.status == "Delivered") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rate this Order",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    for (star in 1..5) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rate $star stars",
                                            tint = FrescutTextMuted,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clickable { onRateOrder(star) }
                                                .padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pink Total Paid highlight box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FrescutRedLightBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Payable",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = formatInr(order.totalAmount),
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRowItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = FrescutTextSecondary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            color = FrescutTextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
