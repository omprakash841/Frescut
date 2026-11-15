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
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
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
import com.example.data.CouponOfferModel
import com.example.data.CustomerReviewModel
import com.example.data.ProductEntity
import com.example.data.PromotionalBannerModel
import com.example.data.QuickNavCategoryItem
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
    banners: List<PromotionalBannerModel>,
    recipes: List<RecipeCardModel>,
    reviews: List<CustomerReviewModel>,
    activeCoupons: List<CouponOfferModel>,
    searchPlaceholders: List<String>,
    quickCategories: List<QuickNavCategoryItem>,
    onLocationClick: () -> Unit,
    onProClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit = {},
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
            products.filter {
                it.category.equals(selectedHomeCategory, ignoreCase = true) ||
                    (selectedHomeCategory.equals("Fish", ignoreCase = true) && it.category.contains("Fish", ignoreCase = true))
            }
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

    val cartCount = remember(cartItems) {
        cartItems.sumOf { it.quantity }
    }

    val activeBanners = remember(banners) {
        banners.filter { it.active }.sortedBy { it.order }
    }

    val activeRecipes = remember(recipes) {
        recipes.filter { it.active }.sortedBy { it.order }
    }

    val activeReviews = remember(reviews) {
        reviews.filter { it.active }.sortedBy { it.order }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
    ) {
        // 1 & 2. Sticky Header (Location/Pro/Bell/Cart collapses on scroll; Search Bar + Category Nav stay sticky)
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
            onCategorySelect = onHomeCategorySelect,
            cartCount = cartCount,
            onCartClick = onCartClick,
            searchPlaceholders = searchPlaceholders,
            quickCategories = quickCategories
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

            // 4. Promotional Banner Carousel (shown ONLY when at least 1 active banner exists in Firestore)
            if (activeBanners.isNotEmpty()) {
                item(key = "promo_banner_carousel") {
                    PromotionalBannerCarousel(
                        banners = activeBanners,
                        onBannerClick = { banner ->
                            onOpenCategoryDetail(banner.targetCategory)
                        }
                    )
                }
            }

            // 5. Active Coupon Banner (read strictly from Firestore "coupons" collection where active == true)
            val featuredCoupon = activeCoupons.firstOrNull { it.active }
            if (featuredCoupon != null) {
                item(key = "welcome_rewards_banner") {
                    WelcomeRewardsBanner(
                        coupon = featuredCoupon,
                        onApplyCode = { onApplyWelcomeCoupon(featuredCoupon.code) }
                    )
                }
            }

            // 6. Category-specific Section OR "Our Current Hits" Section
            item(key = "current_hits_section") {
                val isSpecificCategory = !selectedHomeCategory.equals("All", ignoreCase = true)
                val sectionTitle = if (isSpecificCategory) selectedHomeCategory else "Our Current Hits"
                val sectionSubtitle = if (isSpecificCategory) {
                    "${com.example.ui.theme.formatFreshCutCount(filteredProducts.size)} available"
                } else {
                    "Here’s what everyone’s eating!"
                }
                val showGridLayout = isSpecificCategory || showAllCurrentHitsGrid || currentHitsProducts.size <= 2

                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeaderRow(
                        title = sectionTitle,
                        subtitle = sectionSubtitle,
                        actionText = if (isSpecificCategory) {
                            "Subcategories"
                        } else if (showAllCurrentHitsGrid) {
                            "Carousel"
                        } else {
                            "View All"
                        },
                        onActionClick = {
                            if (isSpecificCategory) {
                                onOpenCategoryDetail(selectedHomeCategory)
                            } else {
                                onToggleViewAllCurrentHits(!showAllCurrentHitsGrid)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!showGridLayout) {
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
                                        cardWidth = androidx.compose.ui.unit.Dp.Unspecified,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Full 2-Column Grid so even 1 or 2 products in a category display properly
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
                                            cardWidth = androidx.compose.ui.unit.Dp.Unspecified,
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

            // 8. Shop by Category Section
            item(key = "shop_by_category_section") {
                Spacer(modifier = Modifier.height(22.dp))
                HomeShopByCategorySection(
                    categories = SeedData.categories,
                    onCategoryClick = { category ->
                        onOpenCategoryDetail(category.name)
                    }
                )
            }

            // 9. Recipe or Meal Inspiration Cards (shown ONLY when at least 1 active recipe exists in Firestore)
            if (activeRecipes.isNotEmpty()) {
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
                        items(activeRecipes, key = { it.id }) { recipe ->
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
            }

            // 10. Customer Reviews Section (shown ONLY when at least 1 active review exists in Firestore)
            if (activeReviews.isNotEmpty()) {
                item(key = "customer_reviews_section") {
                    Spacer(modifier = Modifier.height(22.dp))
                    SectionHeaderRow(
                        title = "Customer Reviews",
                        subtitle = "Verified customer feedback",
                        actionText = null,
                        onActionClick = {}
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeReviews, key = { it.id }) { review ->
                            CustomerReviewCard(review = review)
                        }
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
            .border(1.dp, Color(0xFFC8101E), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Showing $categoryName (${com.example.ui.theme.formatFreshCutCount(itemCount)})",
            color = FrescutRedDark,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subcategories",
                color = FrescutRedBright,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.clickable(onClick = onOpenFullCategory)
            )
            Text(
                text = "Reset",
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                softWrap = false,
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
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Auto-slide every 5 seconds when more than 1 banner exists; pause on touch/drag/swipe and restart 5s timer after release
    LaunchedEffect(banners.size, isDragged, pagerState.settledPage) {
        if (banners.size > 1 && !isDragged) {
            delay(5000L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

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
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF590206),
                                Color(0xFF8B050B),
                                Color(0xFFA50910)
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
                        .fillMaxWidth(0.46f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )

                // Strong dark gradient backdrop behind left text so text never clashes with photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4C0205),
                                    Color(0xFF6E0408).copy(alpha = 0.96f),
                                    Color(0xFF7D0409).copy(alpha = 0.78f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 720f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.58f)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = banner.title,
                            color = FrescutWhite,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp,
                                lineHeight = 20.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (banner.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = banner.subtitle,
                                color = FrescutWhite.copy(alpha = 0.92f),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Checkmark highlights row if present
                    if (banner.highlights.isNotEmpty() && banner.subtitle.isBlank()) {
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
                                            fontSize = 10.sp
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
                    }

                    // White button "Order Now ->" (no shadow)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrescutWhite,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = banner.ctaText,
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
                            if (isSelected) FrescutRedBright else FrescutBorderLight
                        )
                )
            }
        }
    }
}

@Composable
private fun WelcomeRewardsBanner(
    coupon: CouponOfferModel,
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
                    text = coupon.title,
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
                            append("${coupon.discountPercent}% OFF")
                        }
                    }
                )
                if (coupon.subtitle.isNotBlank()) {
                    Text(
                        text = coupon.subtitle,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFCE2E4))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Code: ${coupon.code}",
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
                contentDescription = coupon.title,
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, FrescutBorderLight, RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                    color = Color.Black.copy(alpha = 0.68f),
                    shadowElevation = 0.dp
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
                            .border(1.dp, Color(0xFFC8101E), RoundedCornerShape(8.dp))
                            .clickable(onClick = onAddCutClick)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ Add Cut",
                            color = Color(0xFFC8101E),
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
            .clip(RoundedCornerShape(12.dp))
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
                .fillMaxWidth(0.46f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 12.dp)),
            contentScale = ContentScale.Crop
        )

        // Yellow "%" badge in top-right
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
                .fillMaxWidth(0.56f)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Combo Packs",
                    color = FrescutWhite,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Save More on\nYour Favorites",
                    color = FrescutWhite.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FrescutWhite,
                shadowElevation = 0.dp
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, FrescutBorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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

                if (review.orderedCut.isNotBlank()) {
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
                    if (review.location.isNotBlank()) {
                        Text(
                            text = review.location,
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }
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
