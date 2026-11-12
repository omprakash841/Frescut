package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CartProductItem
import com.example.data.CategoryCardModel
import com.example.data.CustomerReviewModel
import com.example.data.ProductEntity
import com.example.data.PromotionalBannerModel
import com.example.data.RecipeCardModel
import com.example.data.SeedData
import com.example.ui.components.FrescutStickyHeader
import com.example.ui.components.ObserveSmartBottomNavScroll
import com.example.ui.components.ReferenceVerticalProductCard
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedDark
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedPrimary
import com.example.ui.theme.FrescutRedSoftBorder
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite

@Composable
fun HomeScreen(
    areaName: String,
    deliveryMins: Int,
    searchQuery: String,
    selectedHomeCategory: String,
    showAllCurrentHitsGrid: Boolean,
    hasUnreadNotifications: Boolean,
    products: List<ProductEntity>,
    cartItems: List<CartProductItem>,
    onLocationClick: () -> Unit,
    onProClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchBarClick: () -> Unit,
    onHomeCategorySelect: (String) -> Unit,
    onToggleViewAllCurrentHits: (Boolean) -> Unit,
    onOpenCategoryDetail: (String) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onDecrementProduct: (ProductEntity) -> Unit,
    onToggleFavorite: (ProductEntity) -> Unit,
    onApplyWelcomeCoupon: (String) -> Unit,
    onRecipeClick: (RecipeCardModel) -> Unit,
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    ObserveSmartBottomNavScroll(
        listState = listState,
        onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
    )

    // Detect scroll to collapse top bar while keeping Search Bar + Category Nav sticky
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 36
        }
    }

    val filteredProducts = remember(products, selectedHomeCategory) {
        if (selectedHomeCategory.equals("All", ignoreCase = true)) {
            products
        } else {
            products.filter { it.category.equals(selectedHomeCategory, ignoreCase = true) }
        }
    }

    val currentHitsProducts = remember(filteredProducts, products, selectedHomeCategory) {
        val hits = filteredProducts.filter { it.isCurrentHit }
        if (hits.isNotEmpty()) hits else filteredProducts.take(6)
    }

    val budgetProducts = remember(products) {
        products.filter { it.price <= 299 }
    }

    val qtyMap = remember(cartItems) {
        cartItems.associate { it.product.id to it.quantity }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
    ) {
        // 1 & 2. Sticky Header (Location/Pro/Bell collapses on scroll; Search Bar + Category Nav stay sticky)
        FrescutStickyHeader(
            areaName = areaName,
            deliveryMins = deliveryMins,
            searchQuery = searchQuery,
            isScrolled = isScrolled,
            showCategoryBar = true,
            selectedCategory = selectedHomeCategory,
            hasUnreadNotifications = hasUnreadNotifications,
            onLocationClick = onLocationClick,
            onProClick = onProClick,
            onNotificationClick = onNotificationClick,
            onSearchBarClick = onSearchBarClick,
            onCategorySelect = onHomeCategorySelect
        )

        // Scrollable Home Content underneath sticky Search + Category bar
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(FrescutWhite)
                .testTag("home_lazy_column"),
            contentPadding = PaddingValues(bottom = 148.dp)
        ) {
            // 3. Secondary Category/Content Bar when a specific category tab is active on Home
            if (!selectedHomeCategory.equals("All", ignoreCase = true)) {
                item(key = "secondary_category_strip") {
                    SecondaryCategoryFilterBanner(
                        categoryName = selectedHomeCategory,
                        itemCount = filteredProducts.size,
                        onResetAll = { onHomeCategorySelect("All") },
                        onOpenFullCategory = { onOpenCategoryDetail(selectedHomeCategory) }
                    )
                }
            }

            // 4. Promotional Banner Carousel (hidden when user toggled "View All" compact hits or scrolled if desired, visible in All)
            item(key = "promo_banner_carousel") {
                PromotionalBannerCarousel(
                    banners = SeedData.promotionalBanners,
                    onBannerClick = { banner ->
                        onOpenCategoryDetail(banner.targetCategory)
                    }
                )
            }

            // 5. Welcome Rewards Banner (Extra 20% OFF on your first 3 orders! Code: FRESH20)
            item(key = "welcome_rewards_banner") {
                WelcomeRewardsBanner(
                    onApplyCode = { onApplyWelcomeCoupon("FRESH20") }
                )
            }

            // 6. Our Current Hits Section (Supports both Horizontal Carousel and 2-Column Grid matching both screenshots!)
            item(key = "current_hits_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeaderRow(
                        title = "Our Current Hits",
                        subtitle = "Here’s what everyone’s eating!",
                        actionText = if (showAllCurrentHitsGrid) "Carousel" else "View All",
                        onActionClick = { onToggleViewAllCurrentHits(!showAllCurrentHitsGrid) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!showAllCurrentHitsGrid) {
                        // Horizontal Carousel + 2x2 featured grid preview
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentHitsProducts, key = { "hit_${it.id}" }) { product ->
                                ReferenceVerticalProductCard(
                                    product = product,
                                    quantityInCart = qtyMap[product.id] ?: 0,
                                    onCardClick = { onProductClick(product) },
                                    onAddClick = { onAddProduct(product) },
                                    onDecrementClick = { onDecrementProduct(product) },
                                    onFavoriteToggle = { onToggleFavorite(product) },
                                    cardWidth = 172.dp
                                )
                            }
                        }

                        // Also show second row of 2 cards when scrolling to match the 2x2 "On Scroll" reference!
                        if (currentHitsProducts.size >= 4) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                currentHitsProducts.drop(2).take(2).forEach { product ->
                                    ReferenceVerticalProductCard(
                                        product = product,
                                        quantityInCart = qtyMap[product.id] ?: 0,
                                        onCardClick = { onProductClick(product) },
                                        onAddClick = { onAddProduct(product) },
                                        onDecrementClick = { onDecrementProduct(product) },
                                        onFavoriteToggle = { onToggleFavorite(product) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Full 2-Column Grid of all products in Current Hits
                        val rows = filteredProducts.chunked(2)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rows.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { product ->
                                        ReferenceVerticalProductCard(
                                            product = product,
                                            quantityInCart = qtyMap[product.id] ?: 0,
                                            onCardClick = { onProductClick(product) },
                                            onAddClick = { onAddProduct(product) },
                                            onDecrementClick = { onDecrementProduct(product) },
                                            onFavoriteToggle = { onToggleFavorite(product) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Budget Shopping Section ("Cravings on a Budget")
            if (budgetProducts.isNotEmpty()) {
                item(key = "budget_shopping_section") {
                    Spacer(modifier = Modifier.height(22.dp))
                    SectionHeaderRow(
                        title = "Cravings on a Budget",
                        subtitle = "Pocket-friendly fresh cuts under ₹299",
                        actionText = "View All",
                        onActionClick = { onOpenCategoryDetail("Chicken") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(budgetProducts, key = { "budget_${it.id}" }) { product ->
                            ReferenceVerticalProductCard(
                                product = product,
                                quantityInCart = qtyMap[product.id] ?: 0,
                                onCardClick = { onProductClick(product) },
                                onAddClick = { onAddProduct(product) },
                                onDecrementClick = { onDecrementProduct(product) },
                                onFavoriteToggle = { onToggleFavorite(product) },
                                cardWidth = 168.dp
                            )
                        }
                    }
                }
            }

            // 8. Shop by Category Section (matching circular pastel avatars & badged plates in reference screenshots)
            item(key = "shop_by_category_section") {
                Spacer(modifier = Modifier.height(22.dp))
                HomeShopByCategorySection(
                    categories = SeedData.categories,
                    onCategoryClick = { category ->
                        onOpenCategoryDetail(category.name)
                    }
                )
            }

            // 9. Recipe or Meal Inspiration Cards ("Crafted for Your Recipes")
            item(key = "crafted_recipes_section") {
                Spacer(modifier = Modifier.height(22.dp))
                SectionHeaderRow(
                    title = "Crafted for Your Recipes",
                    subtitle = "Chef-curated cuts for restaurant dishes at home",
                    actionText = null,
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(SeedData.recipes, key = { it.id }) { recipe ->
                        RecipeInspirationCard(
                            recipe = recipe,
                            onCardClick = { onRecipeClick(recipe) },
                            onAddCutClick = {
                                val target = products.find { it.id == recipe.primaryProductId }
                                if (target != null) {
                                    onAddProduct(target)
                                }
                            }
                        )
                    }
                }
            }

            // 10. Promotional Content Banner (Combo Packs Banner matching reference)
            item(key = "combo_packs_promo_banner") {
                Spacer(modifier = Modifier.height(22.dp))
                ComboPacksBannerCard(
                    onExploreClick = { onOpenCategoryDetail("Value Packs") },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            // 11. Customer Reviews Section
            item(key = "customer_reviews_section") {
                Spacer(modifier = Modifier.height(22.dp))
                SectionHeaderRow(
                    title = "Customer Reviews",
                    subtitle = "What Bargarh & Odisha meat lovers are saying",
                    actionText = null,
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(SeedData.customerReviews, key = { it.id }) { review ->
                        CustomerReviewCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryCategoryFilterBanner(
    categoryName: String,
    itemCount: Int,
    onResetAll: () -> Unit,
    onOpenFullCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FrescutRedLightBg)
            .border(1.dp, FrescutRedSoftBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Showing $categoryName ($itemCount fresh cuts)",
            color = FrescutRedDark,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Subcategories →",
                color = FrescutRedBright,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.clickable(onClick = onOpenFullCategory)
            )
            Text(
                text = "Reset",
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(onClick = onResetAll)
            )
        }
    }
}

@Composable
private fun PromotionalBannerCarousel(
    banners: List<PromotionalBannerModel>,
    onBannerClick: (PromotionalBannerModel) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 14.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7A0409),
                                Color(0xFFA50910),
                                Color(0xFF8B050B)
                            )
                        )
                    )
                    .clickable { onBannerClick(banner) }
                    .testTag("promo_banner_${banner.id}")
            ) {
                // Right side Food Photography
                Image(
                    painter = painterResource(id = banner.imageRes),
                    contentDescription = banner.title,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.54f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )

                // Smooth dark-red gradient overlay on left so text is 100% legible and matches reference
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7D0409),
                                    Color(0xFF8E060B).copy(alpha = 0.92f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 620f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.66f)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = banner.title,
                        color = FrescutWhite,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            lineHeight = 22.sp
                        )
                    )

                    // Checkmark highlights row: ✔ Hygienic | ✔ Fresh | ✔ Trusted
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        banner.highlights.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = FrescutGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item,
                                    color = FrescutWhite,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                            if (index < banner.highlights.lastIndex) {
                                Text(
                                    text = "|",
                                    color = FrescutWhite.copy(alpha = 0.45f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // White pill button "Order Now ->"
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = FrescutWhite,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = banner.ctaText,
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = FrescutRedBright,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pager Indicator Dots (Red pill for active, grey dots for inactive)
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 16.dp else 8.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) FrescutRedBright else Color(0xFFE5E7EB)
                        )
                )
            }
        }
    }
}

@Composable
private fun WelcomeRewardsBanner(
    onApplyCode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFF5F5), Color(0xFFFDE8E9))
                )
            )
            .border(
                width = 1.dp,
                color = FrescutRedSoftBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onApplyCode)
            .testTag("welcome_rewards_banner")
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome Rewards",
                    color = FrescutRedBright,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = FrescutTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        ) {
                            append("Extra ")
                        }
                        withStyle(
                            SpanStyle(
                                color = FrescutTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        ) {
                            append("20% OFF")
                        }
                    }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = FrescutTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        ) {
                            append("on your first ")
                        }
                        withStyle(
                            SpanStyle(
                                color = FrescutRedBright,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            )
                        ) {
                            append("3 orders!")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFCE2E4))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Code: FRESH20",
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.img_gift_box_reward),
                contentDescription = "Welcome Rewards Gift Box",
                modifier = Modifier
                    .width(96.dp)
                    .height(78.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    subtitle: String?,
    actionText: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        if (!actionText.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .testTag("view_all_${title.lowercase().replace(" ", "_")}")
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionText,
                    color = FrescutRedBright,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = actionText,
                    tint = FrescutRedBright,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeShopByCategorySection(
    categories: List<CategoryCardModel>,
    onCategoryClick: (CategoryCardModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderRow(
            title = "Shop by Category",
            subtitle = "Freshest meats and much more!",
            actionText = null,
            onActionClick = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Circular Pastel Category Avatars Row matching Home screenshot + Grid Row 2
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categories, key = { "home_cat_${it.id}" }) { cat ->
                Column(
                    modifier = Modifier
                        .width(74.dp)
                        .clickable { onCategoryClick(cat) }
                        .testTag("home_category_circle_${cat.id.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .align(Alignment.BottomCenter)
                                .clip(CircleShape)
                                .background(cat.backgroundColor)
                                .border(
                                    width = 1.dp,
                                    color = FrescutRedSoftBorder.copy(alpha = 0.45f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = cat.imageRes),
                                contentDescription = cat.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Optional Top Badge matching Screenshot 1 (e.g., DOUBT-FREE, G.O.L.D., READY BITES)
                        if (!cat.badgeText.isNullOrBlank() && cat.badgeText != "%") {
                            val isGold = cat.badgeText.contains("G.O.L.D")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isGold) FrescutGold else FrescutRedBright
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = cat.badgeText,
                                    color = if (isGold) FrescutTextPrimary else FrescutWhite,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 7.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = cat.name,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeInspirationCard(
    recipe: RecipeCardModel,
    onCardClick: () -> Unit,
    onAddCutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(236.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick)
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrescutBorderLight.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
            ) {
                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.68f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = FrescutGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${recipe.prepTime} • ${recipe.difficulty}",
                            color = FrescutWhite,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recipe.title,
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = recipe.cutName,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${recipe.cutPrice}",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrescutRedLightBg)
                            .border(1.dp, FrescutRedBright, RoundedCornerShape(8.dp))
                            .clickable(onClick = onAddCutClick)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ Add Cut",
                            color = FrescutRedBright,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComboPacksBannerCard(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(FrescutRedDark, FrescutRedPrimary)
                )
            )
            .clickable(onClick = onExploreClick)
            .testTag("combo_packs_banner")
    ) {
        // Right side meat bundle photo
        Image(
            painter = painterResource(id = R.drawable.img_combo_packs_meat),
            contentDescription = "Combo Packs",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.52f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 12.dp)),
            contentScale = ContentScale.Crop
        )

        // Yellow "%" badge in top-right matching Categories reference screenshot
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(FrescutGold),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.58f)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Combo Packs",
                    color = FrescutWhite,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Save More on\nYour Favorites",
                    color = FrescutWhite.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = FrescutWhite
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Packs",
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = FrescutRedBright,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerReviewCard(
    review: CustomerReviewModel
) {
    Card(
        modifier = Modifier
            .width(262.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrescutBorderLight.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = FrescutGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${review.rating}",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FrescutRedLightBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = review.orderedCut,
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "“${review.reviewText}”",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.customerName,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = review.location,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = FrescutGreenDiscount,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Verified",
                        color = FrescutGreenDiscount,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
