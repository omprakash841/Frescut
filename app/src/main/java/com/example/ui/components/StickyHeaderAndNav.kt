package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.QuickNavCategoryItem
import com.example.data.SeedData
import com.example.ui.MainNavTab
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedDark
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedPrimary
import com.example.ui.theme.FrescutRedSoftBorder
import com.example.ui.theme.FrescutSelectedTabUnderline
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutThinDivider
import com.example.ui.theme.FrescutWhite
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Tracks LazyListState scroll position & direction with threshold filtering to control
 * Smart Bottom Navigation visibility:
 * - Always visible at the top of the page or on short non-scrollable pages.
 * - Hides smoothly on downward scroll beyond threshold.
 * - Shows smoothly on upward scroll beyond threshold.
 * - Keeps current state when user stops scrolling.
 */
@Composable
fun ObserveSmartBottomNavScroll(
    listState: LazyListState,
    onBottomNavVisibilityChanged: (Boolean) -> Unit
) {
    var previousIndex by remember(listState) { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var previousScrollOffset by remember(listState) { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.canScrollForward to listState.canScrollBackward
            )
        }.collect { (currentIndex, currentOffset, scrollCaps) ->
            val (canScrollForward, canScrollBackward) = scrollCaps

            // 1. At top of page or short page where scrolling is unnecessary -> must stay visible
            if (!canScrollBackward || (currentIndex == 0 && currentOffset < 16) || (!canScrollForward && !canScrollBackward)) {
                onBottomNavVisibilityChanged(true)
                previousIndex = currentIndex
                previousScrollOffset = currentOffset
                return@collect
            }

            // 2. Compare against previous scroll position with threshold to avoid flicker
            if (currentIndex != previousIndex) {
                if (currentIndex > previousIndex) {
                    // Scrolled down to next item
                    onBottomNavVisibilityChanged(false)
                } else {
                    // Scrolled up to previous item
                    onBottomNavVisibilityChanged(true)
                }
                previousIndex = currentIndex
                previousScrollOffset = currentOffset
            } else {
                val delta = currentOffset - previousScrollOffset
                val thresholdPx = 18
                if (abs(delta) >= thresholdPx) {
                    if (delta > 0) {
                        // Scrolling down
                        onBottomNavVisibilityChanged(false)
                    } else {
                        // Scrolling up
                        onBottomNavVisibilityChanged(true)
                    }
                    previousScrollOffset = currentOffset
                }
            }
        }
    }
}

/**
 * NestedScrollConnection that accumulates touch scroll deltas and triggers Smart Bottom Nav
 * hide/show with a threshold to prevent micro-scroll flickering.
 */
@Composable
fun rememberSmartNavNestedScrollConnection(
    onBottomNavVisibilityChanged: (Boolean) -> Unit
): NestedScrollConnection {
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }
    val thresholdPx = 24f

    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if ((dy > 0f && accumulatedDelta < 0f) || (dy < 0f && accumulatedDelta > 0f)) {
                    accumulatedDelta = 0f
                }
                accumulatedDelta += dy
                if (accumulatedDelta <= -thresholdPx) {
                    // User finger moving up -> scrolling down content -> hide bottom nav
                    onBottomNavVisibilityChanged(false)
                    accumulatedDelta = 0f
                } else if (accumulatedDelta >= thresholdPx) {
                    // User finger moving down -> scrolling up content -> show bottom nav
                    onBottomNavVisibilityChanged(true)
                    accumulatedDelta = 0f
                }
                return Offset.Zero
            }
        }
    }
}

@Composable
fun FrescutStickyHeader(
    areaName: String,
    deliveryMins: Int,
    searchQuery: String,
    isScrolled: Boolean,
    showCategoryBar: Boolean,
    selectedCategory: String,
    hasUnreadNotifications: Boolean,
    onLocationClick: () -> Unit,
    onProClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchBarClick: () -> Unit,
    onCategorySelect: (String) -> Unit,
    cartCount: Int = 0,
    onCartClick: () -> Unit = {},
    searchPlaceholders: List<String> = emptyList(),
    quickCategories: List<QuickNavCategoryItem> = SeedData.defaultQuickCategories,
    modifier: Modifier = Modifier
) {
    val activePlaceholderNames = remember(searchPlaceholders) {
        val rawList = if (searchPlaceholders.isNotEmpty()) {
            searchPlaceholders
        } else {
            SeedData.defaultSearchPlaceholders
        }
        rawList.mapNotNull { term ->
            val cleaned = term.trim()
                .removePrefix("Search for")
                .removePrefix("search for")
                .removeSuffix("...")
                .trim()
            cleaned.takeIf { it.isNotEmpty() }
        }.ifEmpty { SeedData.defaultSearchPlaceholders }
    }

    var placeholderIndex by remember(activePlaceholderNames) { mutableIntStateOf(0) }

    LaunchedEffect(activePlaceholderNames) {
        if (activePlaceholderNames.size > 1) {
            while (true) {
                delay(2500L)
                placeholderIndex = (placeholderIndex + 1) % activePlaceholderNames.size
            }
        }
    }

    val currentPlaceholderName = activePlaceholderNames.getOrElse(placeholderIndex) { "Chicken" }
    val categoriesToDisplay = remember(quickCategories) {
        if (quickCategories.isNotEmpty()) quickCategories else SeedData.defaultQuickCategories
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FrescutWhite)
    ) {
        // Red Header Block (Collapses Top Info on scroll, NEVER hides the Search Bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FrescutRedDark, FrescutRedPrimary)
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 14.dp, vertical = if (isScrolled) 8.dp else 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Expanded Location + Pro + Notification Row (collapses smoothly on scroll)
                AnimatedVisibility(
                    visible = !isScrolled,
                    enter = expandVertically(animationSpec = tween(220)) + fadeIn(tween(180)),
                    exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(150))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Location & Delivery Info (kept as-is, without vertical "|" divider)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onLocationClick)
                                    .testTag("header_location_button")
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Delivery Location",
                                    tint = FrescutWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = areaName,
                                            color = FrescutWhite,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Change Location",
                                            tint = FrescutWhite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = FrescutWhite.copy(alpha = 0.92f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            ) {
                                                append("Delivery in ")
                                            }
                                            withStyle(
                                                SpanStyle(
                                                    color = FrescutGold,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            ) {
                                                append("$deliveryMins min ⚡")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // FRESCUT Pro Pill Button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF6B0408).copy(alpha = 0.65f))
                                    .border(
                                        width = 1.dp,
                                        color = FrescutGold.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable(onClick = onProClick)
                                    .testTag("header_pro_button")
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = "FRESCUT Pro",
                                    tint = FrescutGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = FrescutWhite,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        ) {
                                            append("FRESCUT ")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                color = FrescutGold,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        ) {
                                            append("Pro")
                                        }
                                    },
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Notification Bell with Red Unread Dot
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onNotificationClick)
                                    .testTag("header_notification_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = FrescutWhite,
                                    modifier = Modifier.size(23.dp)
                                )
                                if (hasUnreadNotifications) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-5).dp, y = 5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3B30))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Cart Icon with Item Count Badge (hidden when cart is empty)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onCartClick)
                                    .testTag("header_cart_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = FrescutWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (cartCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-1).dp, y = 1.dp)
                                            .size(17.dp)
                                            .clip(CircleShape)
                                            .background(FrescutGold)
                                            .testTag("header_cart_badge"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cartCount.toString(),
                                            color = FrescutTextPrimary,
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

                // Sticky White Rounded-Rectangle Search Bar (1dp grey border, 12dp corners, zero shadow)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, FrescutBorderLight, RoundedCornerShape(12.dp))
                        .clickable(onClick = onSearchBarClick)
                        .testTag("sticky_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    color = FrescutWhite,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = FrescutTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Search for ",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        maxLines = 1
                                    )
                                    AnimatedContent(
                                        targetState = "$currentPlaceholderName...",
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(420)) togetherWith
                                                fadeOut(animationSpec = tween(420))
                                        },
                                        label = "SearchBarPlaceholderNameFade"
                                    ) { animatedName ->
                                        Text(
                                            text = animatedName,
                                            color = FrescutTextSecondary,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("search_bar_animated_placeholder")
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = searchQuery,
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Search",
                            tint = FrescutTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Full-width 1dp light grey (#E0E0E0) line below the search bar block on Home and Categories
        FrescutThinDivider()

        // Sticky Horizontal Category Navigation directly below the search bar (populated from Firestore "categories")
        if (showCategoryBar) {
            AnimatedContent(
                targetState = isScrolled,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(150))
                },
                label = "StickyCategoryBarTransition"
            ) { compactMode ->
                if (!compactMode) {
                    // Expanded Top Category Strip with Photos/Icons + 2dp Red Underline on Selected Tab + 1dp Bottom Grey Line
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FrescutWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FrescutWhite)
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 12.dp, end = 12.dp, top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            categoriesToDisplay.forEach { item ->
                                val isSelected = selectedCategory.equals(item.id, ignoreCase = true)
                                ExpandedCategoryNavTab(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = { onCategorySelect(item.id) }
                                )
                            }
                        }
                        FrescutThinDivider()
                    }
                } else {
                    // Compact Sticky Text Category Tabs on Scroll + 2dp Red Underline on Selected Tab + 1dp Bottom Grey Line
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FrescutWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 12.dp, end = 12.dp, top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            categoriesToDisplay.forEach { item ->
                                val isSelected = selectedCategory.equals(item.id, ignoreCase = true)
                                Column(
                                    modifier = Modifier.width(74.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) FrescutRedLightBg else FrescutWhite)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) FrescutFocusRed else FrescutBorderLight,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onCategorySelect(item.id) }
                                            .testTag("compact_cat_tab_${item.id.lowercase()}")
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.label,
                                            color = if (isSelected) FrescutFocusRed else FrescutTextPrimary,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                fontSize = 11.5.sp,
                                                lineHeight = 13.5.sp
                                            ),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    FrescutSelectedTabUnderline(isSelected = isSelected)
                                }
                            }
                        }
                        FrescutThinDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedCategoryNavTab(
    item: QuickNavCategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(74.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) FrescutRedLightBg else FrescutWhite)
                .border(
                    width = 1.dp,
                    color = if (isSelected) FrescutFocusRed else FrescutBorderLight,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .testTag("expanded_cat_tab_${item.id.lowercase()}")
                .padding(top = 6.dp, bottom = 6.dp, start = 4.dp, end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.isAllTab -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FrescutRedBright.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = item.label,
                                tint = FrescutRedBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    item.isMoreTab -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = item.label,
                                tint = FrescutTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    item.imageRes != null -> {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                color = if (isSelected) FrescutFocusRed else FrescutTextPrimary,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FrescutSelectedTabUnderline(isSelected = isSelected)
    }
}

@Composable
fun StickyCartSummaryBar(
    itemCount: Int,
    totalAmount: Int,
    totalSavings: Int,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = itemCount > 0,
        enter = expandVertically(tween(220)) + fadeIn(tween(180)),
        exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("sticky_cart_bar"),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFC8101E),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewCartClick)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrescutWhite.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = "Cart",
                            tint = FrescutWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "$itemCount ${if (itemCount == 1) "item" else "items"} • ₹$totalAmount",
                            color = FrescutWhite,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp
                            )
                        )
                        if (totalSavings > 0) {
                            Text(
                                text = "You save ₹$totalSavings on this order",
                                color = FrescutGold,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrescutWhite)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Cart",
                        color = Color(0xFFC8101E),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFC8101E),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar separated cleanly from page content:
 * 1. 1dp light grey (#E0E0E0) border-top at the top edge.
 * 2. Very subtle upward shadow (< 4dp, 2dp).
 * 3. White background, straight corners, extending edge-to-edge to the bottom of the screen.
 * 4. Selected tab in red pill, other tabs in grey.
 */
@Composable
fun FrescutBottomNavigationBar(
    currentTab: MainNavTab,
    isVisible: Boolean,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            initialOffsetY = { fullHeight -> fullHeight }
        ) + expandVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            targetOffsetY = { fullHeight -> fullHeight }
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = modifier.testTag("smart_bottom_nav_container")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                ),
            shape = androidx.compose.ui.graphics.RectangleShape,
            color = FrescutWhite,
            shadowElevation = 2.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FrescutWhite)
                    .navigationBarsPadding()
            ) {
                // 1dp light grey (#E0E0E0) top border using common divider component
                FrescutThinDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainNavTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        val activeIcon = when (tab) {
                            MainNavTab.HOME -> Icons.Filled.Home
                            MainNavTab.CATEGORIES -> Icons.Filled.GridView
                            MainNavTab.ORDERS -> Icons.Filled.Receipt
                            MainNavTab.PROFILE -> Icons.Filled.Person
                        }
                        val inactiveIcon = when (tab) {
                            MainNavTab.HOME -> Icons.Outlined.Home
                            MainNavTab.CATEGORIES -> Icons.Outlined.GridView
                            MainNavTab.ORDERS -> Icons.Outlined.Receipt
                            MainNavTab.PROFILE -> Icons.Outlined.Person
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) Color(0xFFC8101E) else Color.Transparent)
                                .clickable { onTabSelected(tab) }
                                .testTag("bottom_nav_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) activeIcon else inactiveIcon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) FrescutWhite else FrescutTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) FrescutWhite else FrescutTextSecondary,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
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
