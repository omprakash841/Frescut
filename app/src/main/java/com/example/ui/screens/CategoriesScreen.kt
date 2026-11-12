package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartProductItem
import com.example.data.CategoryCardModel
import com.example.data.ProductEntity
import com.example.data.SeedData
import com.example.ui.SortOption
import com.example.ui.components.FrescutStickyHeader
import com.example.ui.components.ObserveSmartBottomNavScroll
import com.example.ui.components.ReferenceHorizontalProductCard
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite

@Composable
fun CategoriesScreen(
    areaName: String,
    deliveryMins: Int,
    searchQuery: String,
    hasUnreadNotifications: Boolean,
    selectedCategoryDetail: String?,
    selectedSubcategory: String,
    selectedSortOption: SortOption,
    products: List<ProductEntity>,
    cartItems: List<CartProductItem>,
    onLocationClick: () -> Unit,
    onProClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchBarClick: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onBackFromCategoryDetail: () -> Unit,
    onSelectSubcategory: (String) -> Unit,
    onSelectSortOption: (SortOption) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onDecrementProduct: (ProductEntity) -> Unit,
    onToggleFavorite: (ProductEntity) -> Unit,
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    ObserveSmartBottomNavScroll(
        listState = listState,
        onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
    )
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 36
        }
    }

    val qtyMap = remember(cartItems) {
        cartItems.associate { it.product.id to it.quantity }
    }

    if (selectedCategoryDetail != null) {
        BackHandler(onBack = onBackFromCategoryDetail)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
    ) {
        // Top Red Sticky Header with Location, Pro, Bell & Search Bar (matches both Category screens in reference)
        FrescutStickyHeader(
            areaName = areaName,
            deliveryMins = deliveryMins,
            searchQuery = searchQuery,
            isScrolled = isScrolled,
            showCategoryBar = false,
            selectedCategory = selectedCategoryDetail ?: "All",
            hasUnreadNotifications = hasUnreadNotifications,
            onLocationClick = onLocationClick,
            onProClick = onProClick,
            onNotificationClick = onNotificationClick,
            onSearchBarClick = onSearchBarClick,
            onCategorySelect = {}
        )

        if (selectedCategoryDetail == null) {
            // Left Screen in Reference: 3x3 "Shop by Category" Grid + "Combo Packs" Banner
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(FrescutWhite)
                    .testTag("categories_grid_screen"),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 14.dp,
                    bottom = 148.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "shop_by_category_header") {
                    Text(
                        text = "Shop by Category",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // 3x3 Grid of Pastel Category Cards
                val rows = SeedData.categories.chunked(3)
                items(rows, key = { row -> row.joinToString { it.id } }) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { category ->
                            CategoryGridTile(
                                category = category,
                                onClick = { onSelectCategory(category.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Combo Packs Promotional Banner at the bottom of Categories Overview
                item(key = "categories_combo_packs_banner") {
                    Spacer(modifier = Modifier.height(4.dp))
                    ComboPacksBannerCard(
                        onExploreClick = { onSelectCategory("Value Packs") }
                    )
                }
            }
        } else {
            // Right Screen in Reference: Category Detail Screen (e.g., "Chicken - 28 Items", Popular sort, Subcategory pills, Product list)
            val categoryModel = remember(selectedCategoryDetail) {
                SeedData.categories.find {
                    it.name.equals(selectedCategoryDetail, ignoreCase = true)
                } ?: SeedData.categories.first()
            }

            val categoryProducts = remember(
                products,
                selectedCategoryDetail,
                selectedSubcategory,
                selectedSortOption
            ) {
                val base = products.filter {
                    it.category.equals(selectedCategoryDetail, ignoreCase = true)
                }.ifEmpty { products }

                val subFiltered = if (selectedSubcategory.equals("All", ignoreCase = true)) {
                    base
                } else {
                    base.filter {
                        it.subcategory.contains(selectedSubcategory, ignoreCase = true) ||
                            it.name.contains(selectedSubcategory, ignoreCase = true)
                    }
                }

                when (selectedSortOption) {
                    SortOption.POPULAR -> subFiltered
                    SortOption.PRICE_LOW_HIGH -> subFiltered.sortedBy { it.price }
                    SortOption.PRICE_HIGH_LOW -> subFiltered.sortedByDescending { it.price }
                    SortOption.DISCOUNT -> subFiltered.sortedByDescending { it.discountPercent }
                }
            }

            var sortMenuExpanded by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FrescutWhite)
                    .testTag("category_detail_screen")
            ) {
                // Category Title + Item Count + "⇅ Popular v" Sort Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackFromCategoryDetail,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("category_detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Categories",
                                tint = FrescutTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Column {
                            Text(
                                text = categoryModel.name,
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = categoryModel.itemCountText,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Sort Dropdown Pill ("⇅ Popular v" in red outline matching reference)
                    Box(modifier = Modifier.padding(end = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(FrescutWhite)
                                .border(
                                    width = 1.dp,
                                    color = FrescutRedBright,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { sortMenuExpanded = true }
                                .testTag("category_sort_button")
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapVert,
                                contentDescription = "Sort",
                                tint = FrescutRedBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedSortOption.label,
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = FrescutRedBright,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(FrescutWhite)
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            color = if (option == selectedSortOption) {
                                                FrescutRedBright
                                            } else {
                                                FrescutTextPrimary
                                            },
                                            fontWeight = if (option == selectedSortOption) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },
                                    onClick = {
                                        onSelectSortOption(option)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Horizontal Subcategory Filter Pills ("All", "Breast", "Leg", "Wing", "Thigh", "Boneless")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryModel.subcategories.forEach { sub ->
                        val isSelected = selectedSubcategory.equals(sub, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) FrescutRedBright else FrescutWhite
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FrescutRedBright else FrescutBorderLight,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onSelectSubcategory(sub) }
                                .testTag("subcat_pill_${sub.lowercase()}")
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sub,
                                color = if (isSelected) FrescutWhite else FrescutTextPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical List of Horizontal Product Cards
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FrescutWhite),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 4.dp,
                        bottom = 148.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryProducts, key = { it.id }) { product ->
                        ReferenceHorizontalProductCard(
                            product = product,
                            quantityInCart = qtyMap[product.id] ?: 0,
                            onCardClick = { onProductClick(product) },
                            onAddClick = { onAddProduct(product) },
                            onDecrementClick = { onDecrementProduct(product) },
                            onFavoriteToggle = { onToggleFavorite(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridTile(
    category: CategoryCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(142.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.backgroundColor)
            .clickable(onClick = onClick)
            .testTag("category_tile_${category.id.lowercase()}")
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.imageRes),
                    contentDescription = category.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Red "%" tag badge for Value Packs matching the reference screenshot
                if (category.badgeText == "%") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(FrescutRedBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%",
                            color = FrescutWhite,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = category.name,
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = category.itemCountText,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
