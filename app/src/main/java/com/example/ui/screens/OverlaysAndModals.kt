package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddressEntity
import com.example.data.BudgetCalculator
import com.example.data.CartProductItem
import com.example.data.CategoryBudgetConfigEntity
import com.example.data.CouponOfferModel
import com.example.data.OrderDomainModel
import com.example.data.ProductEntity
import com.example.data.RecipeCardModel
import com.example.data.RecommendationConfigEntity
import com.example.ui.components.ProductAddButtonOrStepper
import com.example.ui.components.ReferenceHorizontalProductCard
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutGreenBg
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedDark
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedSoftBorder
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    product: ProductEntity,
    categoryConfig: CategoryBudgetConfigEntity,
    recommendationConfig: RecommendationConfigEntity,
    pastOrders: List<OrderDomainModel>,
    quantityInCart: Int,
    onDismiss: () -> Unit,
    onAddBudgetToCart: (Int) -> Unit,
    onAddStandardPackClick: () -> Unit,
    onDecrementClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onViewCartClick: () -> Unit,
    onOpenAdminDashboard: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val minBudget = categoryConfig.minBudget
    val increment = categoryConfig.incrementAmount.coerceAtLeast(1)
    val pricePerKg = product.effectivePricePerKg

    // Starts at category minimum budget by default (Chicken: ₹50, Mutton: ₹100, Fish: ₹50, Eggs: ₹50)
    var selectedBudget by remember(product.id, minBudget) {
        mutableIntStateOf(minBudget)
    }

    val estimatedGrams = remember(selectedBudget, pricePerKg) {
        BudgetCalculator.calculateEstimatedGrams(
            selectedBudget = selectedBudget,
            pricePerKg = pricePerKg
        )
    }

    val showRecommendations = remember(
        selectedBudget,
        recommendationConfig.isEnabled,
        recommendationConfig.triggerBudgetAmount
    ) {
        recommendationConfig.isEnabled && selectedBudget >= recommendationConfig.triggerBudgetAmount
    }

    val recommendations = remember(product, categoryConfig, recommendationConfig, pastOrders) {
        BudgetCalculator.generateRecommendations(
            product = product,
            categoryConfig = categoryConfig,
            recommendationConfig = recommendationConfig,
            pastOrders = pastOrders
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .testTag("product_detail_sheet")
        ) {
            // Scrollable Product Detail + Smart Budget Selector Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Product Image Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = product.imageRes),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Admin Dashboard Shortcut Button
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenAdminDashboard)
                                .testTag("product_detail_admin_btn"),
                            shape = CircleShape,
                            color = FrescutWhite.copy(alpha = 0.94f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "Admin Budget Settings",
                                    tint = FrescutTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onFavoriteToggle),
                            shape = CircleShape,
                            color = FrescutWhite.copy(alpha = 0.94f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (product.isFavorite) {
                                        Icons.Filled.Favorite
                                    } else {
                                        Icons.Outlined.FavoriteBorder
                                    },
                                    contentDescription = "Favorite",
                                    tint = if (product.isFavorite) FrescutRedBright else FrescutTextPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = FrescutWhite.copy(alpha = 0.94f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "FLASH ${product.deliveryTimeMins} mins",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product Title + Rating + Price per KG
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹$pricePerKg/kg • ${product.category} (${product.subcategory})",
                            color = FrescutRedBright,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.testTag("price_per_kg_text")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrescutSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = FrescutGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${product.rating}",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (product.budgetSelectorEnabled) {
                    // SMART BUDGET SELECTOR (Replaces Traditional Weight Selection)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_budget_selector_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FrescutRedLightBg.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = FrescutRedSoftBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Select Your Shopping Budget",
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "${product.category} Min: ₹$minBudget • Step: ₹$increment",
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(FrescutWhite)
                                        .border(1.dp, FrescutRedSoftBorder, RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "₹$pricePerKg/kg",
                                        color = FrescutRedDark,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Selected Budget Amount + Minus and Plus Controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(FrescutWhite)
                                    .border(1.dp, FrescutBorderLight, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val canDecrease = selectedBudget > minBudget
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (canDecrease) FrescutRedLightBg else FrescutSurface
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (canDecrease) FrescutRedBright else FrescutBorderLight,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = canDecrease) {
                                            selectedBudget = (selectedBudget - increment).coerceAtLeast(minBudget)
                                        }
                                        .testTag("budget_minus_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Decrease budget by ₹$increment",
                                        tint = if (canDecrease) FrescutRedBright else FrescutTextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "₹$selectedBudget",
                                        color = FrescutRedBright,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 28.sp
                                        ),
                                        modifier = Modifier.testTag("selected_budget_amount")
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Scale,
                                            contentDescription = null,
                                            tint = FrescutGreenDiscount,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Approx. $estimatedGrams g (Estimated)",
                                            color = FrescutGreenDiscount,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            ),
                                            modifier = Modifier.testTag("estimated_quantity_text")
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FrescutRedBright)
                                        .clickable {
                                            selectedBudget += increment
                                        }
                                        .testTag("budget_plus_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Increase budget by ₹$increment",
                                        tint = FrescutWhite,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // 4. Smart Recommended Budget Suggestions (Appears when selectedBudget >= ₹100)
                            AnimatedVisibility(
                                visible = showRecommendations,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .testTag("recommended_for_you_section")
                                ) {
                                    Text(
                                        text = "Recommended for You",
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        recommendations.forEachIndexed { index, rec ->
                                            val isSelectedRec = selectedBudget == rec.budgetAmount
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelectedRec) FrescutRedBright else FrescutWhite
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelectedRec) FrescutRedBright else FrescutRedSoftBorder,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        selectedBudget = rec.budgetAmount
                                                    }
                                                    .testTag("recommendation_chip_$index")
                                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "₹${rec.budgetAmount}",
                                                    color = if (isSelectedRec) FrescutWhite else FrescutTextPrimary,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = "~${rec.estimatedGrams} g",
                                                    color = if (isSelectedRec) {
                                                        FrescutWhite.copy(alpha = 0.92f)
                                                    } else {
                                                        FrescutGreenDiscount
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = rec.tagLabel,
                                                    color = if (isSelectedRec) {
                                                        FrescutGold
                                                    } else {
                                                        FrescutTextSecondary
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.5.sp
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Clear Estimated Quantity & Pricing Policy Disclaimer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FrescutWhite.copy(alpha = 0.85f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = FrescutTextSecondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estimated quantity (~$estimatedGrams g) is an estimate, not a guaranteed weight. Final quantity and payable amount are based on actual weighed product at ₹$pricePerKg/kg. You will never be charged more than your authorized budget (₹$selectedBudget) without confirmation.",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    ),
                                    modifier = Modifier.testTag("estimate_disclaimer_text")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Nutrition & Quality Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityHighlightChip(text = "${product.proteinGrams}g Protein/100g")
                    QualityHighlightChip(text = "0–4°C Chilled")
                    QualityHighlightChip(text = "Antibiotic-Free")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = product.description,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = FrescutDivider)

            // Fixed Bottom Price & Add to Cart Bar (Handled independently from main screen Bottom Navigation)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_detail_bottom_bar"),
                color = FrescutWhite,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.budgetSelectorEnabled) {
                        Column {
                            Text(
                                text = "₹$selectedBudget",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 21.sp
                                )
                            )
                            Text(
                                text = "Est. ~$estimatedGrams g • ₹$pricePerKg/kg",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAddBudgetToCart(selectedBudget) },
                                colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("product_detail_add_budget_btn")
                            ) {
                                Text(
                                    text = if (quantityInCart > 0) {
                                        "Update ₹$selectedBudget (~${estimatedGrams}g)"
                                    } else {
                                        "Add ₹$selectedBudget (~${estimatedGrams}g)"
                                    },
                                    color = FrescutWhite,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            if (quantityInCart > 0) {
                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FrescutRedLightBg)
                                        .border(1.dp, FrescutRedBright, RoundedCornerShape(12.dp))
                                        .clickable {
                                            onDismiss()
                                            onViewCartClick()
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Cart",
                                        color = FrescutRedBright,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Fallback when budgetSelectorEnabled is disabled for this product in Admin Dashboard
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${product.price}",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 21.sp
                                    )
                                )
                                if (product.originalPrice > product.price) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "₹${product.originalPrice}",
                                        color = FrescutTextMuted,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Standard Pack (${product.weightInfo})",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        ProductAddButtonOrStepper(
                            productId = product.id,
                            quantity = quantityInCart,
                            onAddClick = onAddStandardPackClick,
                            onDecrementClick = onDecrementClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityHighlightChip(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FrescutRedLightBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = FrescutRedBright,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = FrescutRedBright,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun SearchOverlayScreen(
    searchQuery: String,
    searchResults: List<ProductEntity>,
    cartItems: List<CartProductItem>,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onDecrementProduct: (ProductEntity) -> Unit,
    onToggleFavorite: (ProductEntity) -> Unit
) {
    BackHandler(onBack = onClose)

    val qtyMap = remember(cartItems) {
        cartItems.associate { it.product.id to it.quantity }
    }

    val popularSearches = listOf(
        "Seekh",
        "Chicken Breast",
        "Mutton",
        "Rohu Fish",
        "Eggs",
        "Prawns",
        "Drumstick",
        "Wings"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("search_overlay_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close Search",
                    tint = FrescutTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrescutSurface)
                    .border(1.dp, FrescutBorderLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = FrescutTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search for 'Seekh', 'Chicken Breast', 'Fish'...",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = FrescutTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search",
                            tint = FrescutTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Popular Search Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            popularSearches.forEach { tag ->
                val isSelected = searchQuery.equals(tag, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) FrescutRedBright else FrescutSurface)
                        .border(
                            1.dp,
                            if (isSelected) FrescutRedBright else FrescutBorderLight,
                            RoundedCornerShape(50)
                        )
                        .clickable { onQueryChange(tag) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (isSelected) FrescutWhite else FrescutTextPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "Popular Fresh Cuts (${searchResults.size})"
                    } else {
                        "Found ${searchResults.size} matching cuts for “$searchQuery”"
                    },
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(searchResults, key = { "search_${it.id}" }) { product ->
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

@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartProductItem>,
    selectedAddress: AddressEntity,
    appliedCoupon: CouponOfferModel?,
    isProMember: Boolean,
    selectedPaymentMethod: String,
    onClose: () -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onDecrementProduct: (ProductEntity) -> Unit,
    onApplyCouponCode: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onChangeAddressClick: () -> Unit,
    onPlaceOrderClick: () -> Unit
) {
    BackHandler(onBack = onClose)

    val itemSubtotal = cartItems.sumOf { it.lineTotal }
    val mrpTotal = cartItems.sumOf { it.lineOriginalTotal }
    val productSavings = (mrpTotal - itemSubtotal).coerceAtLeast(0)
    val couponDiscount = if (appliedCoupon != null && itemSubtotal >= appliedCoupon.minOrderValue) {
        ((itemSubtotal * appliedCoupon.discountPercent) / 100).coerceAtMost(appliedCoupon.maxDiscount)
    } else {
        0
    }
    val deliveryFee = if (isProMember || itemSubtotal >= 499) 0 else 39
    val grandTotal = (itemSubtotal - couponDiscount + deliveryFee).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("cart_checkout_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FrescutTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Your Cart",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "${cartItems.sumOf { it.quantity }} items • Express 30 min delivery",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your FRESCUT Cart is Empty",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore fresh chicken, mutton, fish & farm eggs delivered in 30 minutes.",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright)
                    ) {
                        Text("Browse Fresh Cuts", color = FrescutWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delivery Location Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FrescutSurface)
                            .border(1.dp, FrescutBorderLight, RoundedCornerShape(14.dp))
                            .clickable(onClick = onChangeAddressClick)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = FrescutRedBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Delivering to ${selectedAddress.label} • ${selectedAddress.areaName}",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Arrival in ${selectedAddress.deliveryMins} min ⚡",
                                    color = FrescutGreenDiscount,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Text(
                            text = "Change",
                            color = FrescutRedBright,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                // Cart Items Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FrescutWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            cartItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.product.imageRes),
                                            contentDescription = item.product.name,
                                            modifier = Modifier
                                                .width(64.dp)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.product.name,
                                                color = FrescutTextPrimary,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.effectiveWeightLabel,
                                                color = if (item.isCustomBudget) {
                                                    FrescutGreenDiscount
                                                } else {
                                                    FrescutTextSecondary
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (item.isCustomBudget) {
                                                        FontWeight.SemiBold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                            )
                                            Text(
                                                text = "₹${item.lineTotal}",
                                                color = FrescutTextPrimary,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            )
                                        }
                                    }

                                    ProductAddButtonOrStepper(
                                        productId = item.product.id,
                                        quantity = item.quantity,
                                        onAddClick = { onAddProduct(item.product) },
                                        onDecrementClick = { onDecrementProduct(item.product) }
                                    )
                                }

                                if (index < cartItems.lastIndex) {
                                    HorizontalDivider(color = FrescutDivider)
                                }
                            }
                        }
                    }
                }

                // Welcome Coupon Offer Bar ("FRESH20")
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FrescutRedLightBg)
                            .border(1.dp, FrescutRedBright.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = FrescutRedBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (appliedCoupon != null) {
                                        "${appliedCoupon.code} Applied (Saved ₹$couponDiscount)"
                                    } else {
                                        "Apply Welcome Code: FRESH20"
                                    },
                                    color = FrescutRedBright,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Extra 20% OFF on your first 3 orders",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FrescutWhite)
                                .border(1.dp, FrescutRedBright, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (appliedCoupon != null) {
                                        onRemoveCoupon()
                                    } else {
                                        onApplyCouponCode("FRESH20")
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (appliedCoupon != null) "Remove" else "Apply",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }

                // Bill Summary Card matching Order Details style
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bill Summary",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Total", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("₹$itemSubtotal", color = FrescutTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (couponDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coupon Discount (FRESH20)", color = FrescutGreenDiscount, style = MaterialTheme.typography.bodyMedium)
                                Text("-₹$couponDiscount", color = FrescutGreenDiscount, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = if (deliveryFee == 0) "₹0 (FREE)" else "₹$deliveryFee",
                                color = if (deliveryFee == 0) FrescutGreenDiscount else FrescutTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Packaging Charge", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("₹0", color = FrescutTextPrimary, fontWeight = FontWeight.Bold)
                        }

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
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "₹$grandTotal",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                        if (productSavings + couponDiscount > 0) {
                            Text(
                                text = "Total Savings on this order: ₹${productSavings + couponDiscount}",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Bottom Checkout Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FrescutWhite,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pay via ${selectedPaymentMethod.substringBefore(" ")}",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "₹$grandTotal",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Button(
                        onClick = onPlaceOrderClick,
                        colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("place_order_btn")
                    ) {
                        Text(
                            text = "Place Order →",
                            color = FrescutWhite,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailBottomSheet(
    recipe: RecipeCardModel,
    onDismiss: () -> Unit,
    onAddPrimaryCut: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "${recipe.prepTime} • ${recipe.difficulty} • ${recipe.calories}",
                color = FrescutRedBright,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            recipe.steps.forEachIndexed { idx, step ->
                Text(
                    text = "${idx + 1}. $step",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    onAddPrimaryCut()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add ${recipe.cutName} • ₹${recipe.cutPrice}",
                    color = FrescutWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveOrderTrackingBottomSheet(
    order: OrderDomainModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Cold-Chain Tracking • ${order.orderId}",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = order.etaText,
                        color = FrescutGreenDiscount,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FrescutGreenBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalShipping,
                        contentDescription = null,
                        tint = FrescutGreenDiscount,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Delivery Partner: Debashish Sahu",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Insulated Chiller Bag Temp: 2.4°C (Verified)",
                            color = FrescutGreenDiscount,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FrescutWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Call Rider",
                        tint = FrescutGreenDiscount,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Route: FRESCUT Bargarh Cold Hub → Main Road → Udhepali, Bargarh",
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    addresses: List<AddressEntity>,
    onSelectAddress: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select Delivery Location",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            addresses.forEach { addr ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (addr.isSelected) 1.5.dp else 1.dp,
                            color = if (addr.isSelected) FrescutRedBright else FrescutBorderLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectAddress(addr.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${addr.label} • ${addr.areaName}",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = addr.fullAddress,
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Delivery in ${addr.deliveryMins} min ⚡",
                            color = FrescutGreenDiscount,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    if (addr.isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = FrescutRedBright
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    onDismiss: () -> Unit,
    onApplyWelcomeCode: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = FrescutRedBright
                )
                Text(
                    text = "FRESCUT Updates",
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrescutGreenBg)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Order #FC12847 is Out for Delivery 🚚",
                    color = FrescutGreenDiscount,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Arriving at Udhepali, Bargarh in ~18 mins. Chiller bag temperature verified at 2.4°C.",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrescutRedLightBg)
                    .clickable {
                        onApplyWelcomeCode()
                        onDismiss()
                    }
                    .padding(12.dp)
            ) {
                Text(
                    text = "Welcome Reward Unlocked: Extra 20% OFF 🎁",
                    color = FrescutRedBright,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Tap here to apply code FRESH20 on your next order!",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
