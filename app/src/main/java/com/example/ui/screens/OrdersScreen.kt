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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutGreenBg
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutGreenStep
import com.example.ui.theme.FrescutOrangeBg
import com.example.ui.theme.FrescutOrangeStatus
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import java.text.NumberFormat
import java.util.Locale

private val orderFilterTabs = listOf("All", "Processing", "Out for Delivery", "Delivered")
private val orderStepLabels = listOf(
    "Order\nPlaced",
    "Processing",
    "Packed",
    "Out for\nDelivery",
    "Delivered"
)

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
            onBottomNavVisibilityChanged = onBottomNavVisibilityChanged,
            modifier = modifier
        )
    } else {
        val listState = rememberLazyListState()
        ObserveSmartBottomNavScroll(
            listState = listState,
            onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
        )
        val filteredOrders = remember(orders, selectedStatusFilter) {
            if (selectedStatusFilter.equals("All", ignoreCase = true)) {
                orders
            } else {
                orders.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(FrescutWhite)
                .windowInsetsPadding(WindowInsets.statusBars)
                .testTag("my_orders_screen")
        ) {
            // Top Bar: "My Orders" + Search + Cart with Badge
            OrdersTopAppBar(
                title = "My Orders",
                showBackButton = false,
                cartCount = cartCount,
                onBackClick = {},
                onSearchClick = onSearchClick,
                onCartClick = onCartClick
            )

            // Order Status Filter Pills ("All", "Processing", "Out for Delivery", "Delivered")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderFilterTabs.forEach { tab ->
                    val isSelected = selectedStatusFilter.equals(tab, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) FrescutRedBright else FrescutWhite)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FrescutRedBright else FrescutBorderLight,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSelectStatusFilter(tab) }
                            .testTag("order_filter_${tab.lowercase().replace(" ", "_")}")
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) FrescutWhite else FrescutTextPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }

            // Orders List
            LazyColumn(
                state = listState,
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
                items(filteredOrders, key = { it.orderId }) { order ->
                    OrderSummaryCard(
                        order = order,
                        onCardClick = { onOpenOrderDetail(order.orderId) },
                        onReorderClick = { onReorderClick(order) }
                    )
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
                val badgeDisplay = if (cartCount > 0) cartCount else 3
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
                        text = badgeDisplay.toString(),
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

@Composable
private fun OrderSummaryCard(
    order: OrderDomainModel,
    onCardClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = FrescutBorderLight.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            // Top Row: Status Pill Badge on Left + Order ID & Timestamp on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                OrderStatusPillBadge(status = order.status)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = order.orderId,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.placedAtText,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Row: Product Thumbnails + Item Total + Chevron
            when (order.status) {
                "Out for Delivery" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            order.items.take(4).forEach { item ->
                                Image(
                                    painter = painterResource(id = item.imageRes),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(
                                            1.dp,
                                            FrescutBorderLight,
                                            RoundedCornerShape(24.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View Details",
                            tint = FrescutTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${order.totalItemsCount} ${if (order.totalItemsCount == 1) "Item" else "Items"} • ${formatInr(order.totalAmount)}",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Green ETA Banner: "📍 Arriving in ~ 18 min"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrescutGreenBg)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = FrescutGreenDiscount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.etaText,
                            color = FrescutGreenDiscount,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                "Processing" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            order.items.firstOrNull()?.let { item ->
                                Image(
                                    painter = painterResource(id = item.imageRes),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(
                                            1.dp,
                                            FrescutBorderLight,
                                            RoundedCornerShape(24.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                text = "${order.totalItemsCount} ${if (order.totalItemsCount == 1) "Item" else "Items"} • ${formatInr(order.totalAmount)}",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View Details",
                            tint = FrescutTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compact 5-step progress tracker inside Processing card matching reference
                    OrderStepTimeline(
                        activeStepIndex = 1,
                        timestamps = emptyList(),
                        showTimestamps = false
                    )
                }

                else -> { // "Delivered"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                order.items.take(2).forEach { item ->
                                    Image(
                                        painter = painterResource(id = item.imageRes),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .width(64.dp)
                                            .height(46.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .border(
                                                1.dp,
                                                FrescutBorderLight,
                                                RoundedCornerShape(22.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${order.totalItemsCount} Items • ${formatInr(order.totalAmount)}",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View Details",
                            tint = FrescutTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Warm Cream Rating + Reorder bar matching reference
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrescutCreamRatingBg)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = FrescutGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rated ${order.rating}",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // "↻ Reorder" white pill button with red border
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(FrescutWhite)
                                .border(1.dp, FrescutRedBright, RoundedCornerShape(50))
                                .clickable(onClick = onReorderClick)
                                .testTag("reorder_btn_${order.orderId}")
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = FrescutRedBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reorder",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
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
private fun OrderStatusPillBadge(status: String) {
    val (bgColor, contentColor, icon) = when (status) {
        "Out for Delivery" -> Triple(
            FrescutRedLightBg,
            FrescutRedBright,
            Icons.Filled.LocalShipping
        )
        "Processing" -> Triple(
            FrescutOrangeBg,
            FrescutOrangeStatus,
            Icons.Filled.AccessTime
        )
        else -> Triple(
            FrescutGreenBg,
            FrescutGreenDiscount,
            Icons.Filled.CheckCircle
        )
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = contentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.5.sp
            )
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
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val detailListState = rememberLazyListState()
    ObserveSmartBottomNavScroll(
        listState = detailListState,
        onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
    )

    val activeStepIndex = when (order.status) {
        "Processing" -> 1
        "Out for Delivery" -> 3
        else -> 4
    }

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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
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

                            OrderStatusPillBadge(status = order.status)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5-Step Progress Timeline with Timestamps
                        OrderStepTimeline(
                            activeStepIndex = activeStepIndex,
                            timestamps = order.timelineTimes,
                            showTimestamps = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Green Live Tracking Banner matching right screen of reference
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrescutGreenBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = order.etaText,
                                        color = FrescutGreenDiscount,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = if (order.status == "Delivered") {
                                            "Freshness & cold chain verified"
                                        } else {
                                            "Your order is on the way!"
                                        },
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // "📍 Track Live" white pill button with red outline
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .border(1.dp, FrescutRedBright, RoundedCornerShape(50))
                                    .clickable(
                                        onClick = if (order.status == "Delivered") {
                                            onReorderClick
                                        } else {
                                            onTrackLiveClick
                                        }
                                    )
                                    .testTag("track_live_btn"),
                                shape = RoundedCornerShape(50),
                                color = FrescutWhite
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (order.status == "Delivered") {
                                            Icons.Filled.Refresh
                                        } else {
                                            Icons.Filled.LocationOn
                                        },
                                        contentDescription = null,
                                        tint = FrescutRedBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (order.status == "Delivered") {
                                            "Reorder"
                                        } else {
                                            "Track Live"
                                        },
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

            // 2. Order Items Card ("Order Items (3)")
            item(key = "order_items_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
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
                                HorizontalDivider(
                                    color = FrescutDivider,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Order Summary Card ("Item Total", "Delivery Fee", "Packaging Charge", "Total Paid")
            item(key = "order_summary_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRowItem(
                            label = "Delivery Fee",
                            value = formatInr(order.deliveryFee)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRowItem(
                            label = "Packaging Charge",
                            value = formatInr(order.packagingCharge)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pink Total Paid highlight box matching reference right screenshot
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
                                text = "Total Paid",
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
