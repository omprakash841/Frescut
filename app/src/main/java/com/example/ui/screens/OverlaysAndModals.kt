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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.AppConfigModel
import kotlinx.coroutines.launch
import com.example.ui.components.ProductAddButtonOrStepper
import com.example.ui.components.ReferenceHorizontalProductCard
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutGreenBg
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutLabeledInputField
import com.example.ui.theme.FrescutOutlineChip
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutPaymentOptionCard
import com.example.ui.theme.FrescutPrimaryButton
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedDark
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedSoftBorder
import com.example.ui.theme.FrescutSelectedPinkBg
import com.example.ui.theme.FrescutSectionBandDivider
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutThinDivider
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.formatFreshCutCount
import com.example.ui.theme.formatItemCount
import com.example.ui.theme.frescutOutlineContainer

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
    onViewCartClick: () -> Unit
) {
    BackHandler(onBack = onDismiss)

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

    // Dedicated Full-Screen Product Detail Page (occupies complete mobile viewport)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("product_detail_sheet")
    ) {
        // 1 & 2. Top Navigation Bar: Back Navigation + Category Tag + Action Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("product_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FrescutTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = product.category,
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Fresh Cold-Chain Cut",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                    )
                }
            }
        }

        HorizontalDivider(color = FrescutDivider)

        // Scrollable Full-Screen Product Detail + Smart Budget Selector Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 3. Large Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

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
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${product.deliveryTimeMins} min delivery",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4, 5, 6, 7. Product Name, Price per KG, Category & Rating (shown only when rating > 0 and reviewCount > 0)
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
                            fontSize = 21.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹$pricePerKg/kg • ${product.category} (${product.subcategory})",
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.testTag("price_per_kg_text")
                    )
                }

                if (product.rating > 0f && product.reviewCount > 0) {
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
                            text = "${product.rating} (${product.reviewCount})",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (product.budgetSelectorEnabled) {
                // 8, 9, 10. SMART BUDGET SELECTOR + ESTIMATED QUANTITY + WEIGHT DISCLAIMER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_budget_selector_card"),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = FrescutBorderLight,
                                shape = FrescutOutlineShape
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
                                        fontSize = 15.5.sp
                                    )
                                )
                                Text(
                                    text = "${product.category} Min: ₹$minBudget • Increment: ₹$increment",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
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

                        // Large Selected Budget Amount + Minus and Plus Controls (48dp touch targets)
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
                                    .size(48.dp)
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
                                            fontSize = 13.5.sp
                                        ),
                                        modifier = Modifier.testTag("estimated_quantity_text")
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
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

                        // Smart Recommended Budget Suggestions (Appears when selectedBudget >= ₹100)
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
                                                .clip(FrescutOutlineShape)
                                                .background(
                                                    if (isSelectedRec) FrescutSelectedPinkBg else FrescutWhite
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelectedRec) FrescutFocusRed else FrescutBorderLight,
                                                    shape = FrescutOutlineShape
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
                                                color = if (isSelectedRec) FrescutFocusRed else FrescutTextPrimary,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.5.sp
                                                )
                                            )
                                            Text(
                                                text = "~${rec.estimatedGrams} g",
                                                color = FrescutGreenDiscount,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                            Text(
                                                text = rec.tagLabel,
                                                color = if (isSelectedRec) {
                                                    FrescutFocusRed
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

                        // 10. Weight Disclaimer (Exact Master Prompt Text)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FrescutWhite.copy(alpha = 0.9f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                text = "Estimated quantity is not guaranteed weight. Final quantity and payable amount are based on actual weighing. The customer will never be charged above their authorized budget without confirmation.",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 14.5.sp
                                ),
                                modifier = Modifier.testTag("estimate_disclaimer_text")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 11. Product Quality Information
            Text(
                text = "FRESCUT Quality Promise",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityHighlightChip(text = "${product.proteinGrams}g Protein/100g")
                QualityHighlightChip(text = "0–4°C Chilled")
                QualityHighlightChip(text = "Antibiotic-Free")
                QualityHighlightChip(text = "Hygienic Vacuum Pack")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 12. Product Description
            Text(
                text = "About this Cut",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        HorizontalDivider(color = FrescutBorderLight, thickness = 1.dp)

        // 13. Fixed Bottom Purchase Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("product_detail_bottom_bar"),
            color = FrescutWhite,
            shadowElevation = 0.dp
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
                            text = "Approx. $estimatedGrams g (Estimated)",
                            color = FrescutGreenDiscount,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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
                                .height(48.dp)
                                .testTag("product_detail_add_budget_btn")
                        ) {
                            Text(
                                text = "Add ₹$selectedBudget",
                                color = FrescutWhite,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp
                                )
                            )
                        }

                        if (quantityInCart > 0) {
                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FrescutRedLightBg)
                                    .border(1.dp, FrescutRedBright, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onDismiss()
                                        onViewCartClick()
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "View Cart",
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
    searchChips: List<String> = emptyList(),
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
    var isSearchFocused by remember { mutableStateOf(false) }

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
                    .clip(FrescutOutlineShape)
                    .background(FrescutWhite)
                    .border(
                        width = 1.dp,
                        color = if (isSearchFocused) FrescutFocusRed else FrescutBorderLight,
                        shape = FrescutOutlineShape
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = if (isSearchFocused) FrescutFocusRed else FrescutTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search for Chicken, Mutton, Fish, Eggs...",
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
                            .onFocusChanged { isSearchFocused = it.isFocused }
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

        // Popular Search Chips read strictly from Firestore "config" document
        if (searchChips.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                searchChips.forEach { tag ->
                    val isSelected = searchQuery.equals(tag, ignoreCase = true)
                    FrescutOutlineChip(
                        label = tag,
                        isSelected = isSelected,
                        onClick = { onQueryChange(tag) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val searchRows = remember(searchResults) {
            searchResults.chunked(2)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "Available (${formatFreshCutCount(searchResults.size)})"
                    } else {
                        val cutWord = if (searchResults.size == 1) "matching cut" else "matching cuts"
                        "Found ${searchResults.size} $cutWord for “$searchQuery”"
                    },
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(
                items = searchRows,
                key = { row -> "search_row_" + row.joinToString("_") { it.id } }
            ) { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowProducts.forEach { product ->
                        ReferenceHorizontalProductCard(
                            product = product,
                            quantityInCart = qtyMap[product.id] ?: 0,
                            onCardClick = { onProductClick(product) },
                            onAddClick = { onAddProduct(product) },
                            onDecrementClick = { onDecrementProduct(product) },
                            onFavoriteToggle = { onToggleFavorite(product) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartProductItem>,
    selectedAddress: AddressEntity?,
    activeCoupons: List<CouponOfferModel> = emptyList(),
    appliedCoupon: CouponOfferModel?,
    isProMember: Boolean,
    appConfig: AppConfigModel = AppConfigModel(),
    selectedPaymentMethod: String,
    onSelectPaymentMethod: (String) -> Unit = {},
    deliveryInstructions: String = "",
    onDeliveryInstructionsChange: (String) -> Unit = {},
    onClose: () -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onDecrementProduct: (ProductEntity) -> Unit,
    onAdjustBudget: (CartProductItem, Int) -> Unit = { _, _ -> },
    onRemoveProduct: (String) -> Unit = {},
    onApplyCouponCode: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onChangeAddressClick: () -> Unit,
    onOpenAddressScreenClick: () -> Unit = onChangeAddressClick,
    onAddAddress: (String, String, String) -> Unit = { _, _, _ -> },
    onPlaceOrderClick: () -> Unit
) {
    BackHandler(onBack = onClose)

    var couponCodeInput by remember { mutableStateOf("") }
    var isCouponInputFocused by remember { mutableStateOf(false) }

    val totalItemQty = cartItems.sumOf { it.quantity }
    val itemSubtotal = cartItems.sumOf { it.lineTotal }
    val mrpTotal = cartItems.sumOf { it.lineOriginalTotal }.coerceAtLeast(itemSubtotal)
    val productSavings = (mrpTotal - itemSubtotal).coerceAtLeast(0)
    val couponDiscount = if (appliedCoupon != null && itemSubtotal >= appliedCoupon.minOrderValue) {
        ((itemSubtotal * appliedCoupon.discountPercent) / 100).coerceAtMost(appliedCoupon.maxDiscount)
    } else {
        0
    }
    val totalSavings = productSavings + couponDiscount
    val activeDeliveryFee = (appConfig.deliveryFee ?: 0).coerceAtLeast(0)
    val activeHandlingFee = (appConfig.handlingFee ?: 0).coerceAtLeast(0)
    val grandTotal = (
        mrpTotal - totalSavings + activeDeliveryFee + activeHandlingFee
    ).coerceAtLeast(0)
    val hasValidAddress = selectedAddress != null && selectedAddress.fullAddress.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("cart_checkout_screen")
    ) {
        // Top Header (No hardcoded "Express 30 min delivery"; reads deliveryTimeText from config if present)
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
                        text = "Your Cart & Checkout",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    val subtitleText = buildString {
                        append(formatItemCount(totalItemQty))
                        if (appConfig.deliveryTimeText.isNotBlank()) {
                            append(" • ")
                            append(appConfig.deliveryTimeText)
                        }
                    }
                    Text(
                        text = subtitleText,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        FrescutThinDivider()

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
                        text = "Explore fresh chicken, mutton, fish & farm eggs.",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FrescutPrimaryButton(
                        text = "Browse Fresh Cuts",
                        onClick = onClose
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1a. Top Savings Strip: "You have saved ₹X" (shown ONLY when totalSavings > 0)
                if (totalSavings > 0) {
                    item(key = "cart_savings_strip") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .clip(FrescutOutlineShape)
                                .background(FrescutGreenBg)
                                .border(1.dp, FrescutGreenDiscount.copy(alpha = 0.35f), FrescutOutlineShape)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("cart_savings_banner"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "You have saved ₹$totalSavings",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp
                                )
                            )
                            Text(
                                text = formatItemCount(totalItemQty),
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // 1b. Cart Items at the Very Top (photo, name, quantity) in unified 1dp #E0E0E0 bordered cards
                items(cartItems, key = { "cart_item_${it.product.id}" }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .frescutOutlineContainer()
                            .padding(12.dp)
                            .testTag("cart_item_card_${item.product.id}"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Image(
                                    painter = painterResource(id = item.product.imageRes),
                                    contentDescription = item.product.name,
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, FrescutBorderLight, RoundedCornerShape(8.dp)),
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Adjust Budget:",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FrescutWhite)
                                        .border(1.dp, FrescutBorderLight, RoundedCornerShape(6.dp))
                                        .clickable { onAdjustBudget(item, -10) }
                                        .testTag("cart_budget_minus_${item.product.id}")
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "-₹10",
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FrescutSelectedPinkBg)
                                        .border(1.dp, FrescutFocusRed, RoundedCornerShape(6.dp))
                                        .clickable { onAdjustBudget(item, 10) }
                                        .testTag("cart_budget_plus_${item.product.id}")
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "+₹10",
                                        color = FrescutFocusRed,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Text(
                                text = "Remove",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier
                                    .clickable { onRemoveProduct(item.product.id) }
                                    .testTag("cart_remove_${item.product.id}")
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // 8dp thick #F5F5F5 section divider between Items and Coupon
                item(key = "divider_items_coupon") {
                    FrescutSectionBandDivider()
                }

                // 2a. Coupon Code Box (1dp #E0E0E0 border, 12dp radius, white background; red border + pink bg if applied)
                item(key = "cart_coupon_box") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .frescutOutlineContainer(isSelected = appliedCoupon != null)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (appliedCoupon != null) {
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
                                        imageVector = Icons.Filled.LocalOffer,
                                        contentDescription = null,
                                        tint = FrescutFocusRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${appliedCoupon.code} Applied (Saved ₹$couponDiscount)",
                                            color = FrescutFocusRed,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                        if (appliedCoupon.subtitle.isNotBlank()) {
                                            Text(
                                                text = appliedCoupon.subtitle,
                                                color = FrescutTextSecondary,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(FrescutOutlineShape)
                                        .background(FrescutWhite)
                                        .border(1.dp, FrescutFocusRed, FrescutOutlineShape)
                                        .clickable { onRemoveCoupon() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Remove",
                                        color = FrescutFocusRed,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = FrescutRedBright
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(FrescutOutlineShape)
                                        .background(FrescutWhite)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCouponInputFocused) FrescutFocusRed else FrescutBorderLight,
                                            shape = FrescutOutlineShape
                                        )
                                        .padding(horizontal = 10.dp, vertical = 9.dp)
                                ) {
                                    if (couponCodeInput.isEmpty()) {
                                        Text(
                                            text = "Enter coupon code",
                                            color = FrescutTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    BasicTextField(
                                        value = couponCodeInput,
                                        onValueChange = { couponCodeInput = it.uppercase() },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            color = FrescutTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isCouponInputFocused = it.isFocused }
                                            .testTag("checkout_coupon_input")
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(FrescutOutlineShape)
                                        .background(FrescutRedBright)
                                        .clickable {
                                            onApplyCouponCode(couponCodeInput)
                                        }
                                        .testTag("checkout_apply_coupon_btn")
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "Apply",
                                        color = FrescutWhite,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }
                            }

                            if (activeCoupons.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    activeCoupons.forEach { offer ->
                                        FrescutOutlineChip(
                                            label = "${offer.code} (${offer.discountPercent}% OFF)",
                                            isSelected = false,
                                            onClick = { onApplyCouponCode(offer.code) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 8dp thick #F5F5F5 section divider between Coupon and Bill Summary
                item(key = "divider_coupon_bill") {
                    FrescutSectionBandDivider()
                }

                // 2b. Bill Summary: Item total (MRP), Discount (-), Delivery fee (only if > 0), Handling fee (only if > 0), Amount to be paid
                item(key = "cart_bill_summary") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .frescutOutlineContainer()
                            .padding(14.dp)
                            .testTag("checkout_bill_summary_card"),
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
                            Text("Item total", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("₹$mrpTotal", color = FrescutTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = FrescutGreenDiscount, style = MaterialTheme.typography.bodyMedium)
                            Text("-₹$totalSavings", color = FrescutGreenDiscount, fontWeight = FontWeight.Bold)
                        }
                        if (activeDeliveryFee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery fee", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "₹$activeDeliveryFee",
                                    color = FrescutTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (activeHandlingFee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Handling fee", color = FrescutTextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "₹$activeHandlingFee",
                                    color = FrescutTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        FrescutThinDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount to be paid",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "₹$grandTotal",
                                color = FrescutRedBright,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }

                // 8dp thick #F5F5F5 section divider between Bill Summary and Payment Method / Policies
                item(key = "divider_bill_payment") {
                    FrescutSectionBandDivider()
                }

                // Payment Method Options (Each option in a separate bordered card with radio button on the right; only Cash on Delivery)
                item(key = "cart_payment_methods") {
                    val paymentOptions = listOf(
                        "Cash on Delivery"
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Payment Method",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        paymentOptions.forEach { option ->
                            val isSelected = selectedPaymentMethod == option
                            FrescutPaymentOptionCard(
                                title = option,
                                isSelected = isSelected,
                                onClick = { onSelectPaymentMethod(option) },
                                modifier = Modifier.testTag("checkout_payment_${option.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // 8dp thick #F5F5F5 section divider before Policies
                item(key = "divider_payment_policies") {
                    FrescutSectionBandDivider()
                }

                // 3. Policies (3 short bullets: quantity modification, cancellation window, reschedule before dispatch)
                item(key = "cart_policies_section") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .frescutOutlineContainer()
                            .padding(14.dp)
                            .testTag("checkout_policies_card"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Cancellation & Order Policies",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "• Once the order is placed, item quantities cannot be modified.",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
                        )
                        Text(
                            text = "• Order cancellation is allowed only within a few minutes of placing the order.",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
                        )
                        Text(
                            text = "• Orders can be rescheduled only before dispatch.",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
                        )
                    }
                }
            }

            // 4. Bottom Sticky Bar: Left "Deliver to [address or 'Add address']" + payment method below; Right "Place Order" or "Add Address" button
            FrescutThinDivider()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_bottom_sticky_bar"),
                color = FrescutWhite,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenAddressScreenClick() }
                            .padding(end = 12.dp)
                            .testTag("checkout_sticky_address_trigger")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Delivery Address",
                                tint = FrescutRedBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasValidAddress) {
                                    "Deliver to ${selectedAddress!!.label} (${selectedAddress.areaName})"
                                } else {
                                    "Deliver to Add address"
                                },
                                color = if (hasValidAddress) FrescutTextPrimary else FrescutRedBright,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$selectedPaymentMethod • ₹$grandTotal",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            if (!hasValidAddress) {
                                onOpenAddressScreenClick()
                            } else {
                                onPlaceOrderClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                        shape = FrescutOutlineShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("place_order_btn")
                    ) {
                        Text(
                            text = if (!hasValidAddress) "Add Address" else "Place Order",
                            color = FrescutWhite,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Suppress("MissingPermission")
@Composable
fun AddressFormScreen(
    addresses: List<AddressEntity>,
    initialPhone: String = "",
    onSelectAddress: (Int) -> Unit,
    onSaveAddressDetails: (
        label: String,
        houseAndArea: String,
        floor: String,
        landmark: String,
        receiverPhone: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var houseAndArea by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var saveAsLabel by remember { mutableStateOf("Home") }
    var receiverPhone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var isPinSelectorOpen by remember { mutableStateOf(false) }
    var pinLat by remember { mutableStateOf(12.9716) }
    var pinLng by remember { mutableStateOf(77.5946) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var isReverseGeocoding by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val isHouseAreaError = attemptedSubmit && houseAndArea.isBlank()
    val isPhoneError = attemptedSubmit && receiverPhone.isBlank()

    fun fetchCurrentDeviceCoordinates() {
        isFetchingLocation = true
        locationStatusMessage = null
        try {
            val locationManager =
                context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER,
                android.location.LocationManager.PASSIVE_PROVIDER
            )
            var bestLocation: android.location.Location? = null
            if (locationManager != null) {
                for (provider in providers) {
                    val loc = runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            }
            if (bestLocation != null) {
                pinLat = bestLocation.latitude
                pinLng = bestLocation.longitude
                isFetchingLocation = false
                isPinSelectorOpen = true
            } else if (locationManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val activeProvider = providers.firstOrNull {
                    runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false)
                }
                if (activeProvider != null) {
                    locationManager.getCurrentLocation(
                        activeProvider,
                        null,
                        androidx.core.content.ContextCompat.getMainExecutor(context)
                    ) { loc ->
                        isFetchingLocation = false
                        if (loc != null) {
                            pinLat = loc.latitude
                            pinLng = loc.longitude
                        }
                        isPinSelectorOpen = true
                    }
                } else {
                    isFetchingLocation = false
                    isPinSelectorOpen = true
                    locationStatusMessage = "GPS off hai ya signal nahi mila. Pin ko drag karke ya manually address chunein."
                }
            } else {
                isFetchingLocation = false
                isPinSelectorOpen = true
            }
        } catch (e: Exception) {
            isFetchingLocation = false
            isPinSelectorOpen = true
            locationStatusMessage = "Location fetch nahi ho paya. Aap pin adjust ya manual entry kar sakte hain."
        }
    }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentDeviceCoordinates()
        } else {
            locationStatusMessage =
                "Location permission deny ho gaya hai. Aap neeche manually apna address bhar sakte hain."
        }
    }

    fun confirmPinAndReverseGeocode(lat: Double, lng: Double) {
        selectedLat = lat
        selectedLng = lng
        isReverseGeocoding = true
        locationStatusMessage = null
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val geocodedPair: Pair<String, String>? = try {
                if (android.location.Geocoder.isPresent()) {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocation(lat, lng, 1)
                    val first = results?.firstOrNull()
                    if (first != null) {
                        val subLocality = first.subLocality?.trim().orEmpty()
                        val locality = first.locality?.trim().orEmpty()
                        val thoroughfare = first.thoroughfare?.trim().orEmpty()
                        val feature = first.featureName?.trim().orEmpty()
                        val fullLine = first.getAddressLine(0)?.trim().orEmpty()
                        val primaryArea = listOf(feature, thoroughfare, subLocality, locality)
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .joinToString(", ")
                            .ifBlank { fullLine }
                        val nearLandmark = subLocality.ifBlank { locality }
                        primaryArea to nearLandmark
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (unused: Exception) {
                null
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                isReverseGeocoding = false
                isPinSelectorOpen = false
                if (geocodedPair != null && geocodedPair.first.isNotBlank()) {
                    houseAndArea = geocodedPair.first
                    if (landmark.isBlank() && geocodedPair.second.isNotBlank()) {
                        landmark = geocodedPair.second
                    }
                    locationStatusMessage =
                        "Location confirm ho gaya (${String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)}). Aap address edit kar sakte hain."
                } else {
                    locationStatusMessage =
                        "Coordinates save ho gaye (${String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)}). Kripya neeche address details bharein."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("address_form_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("address_form_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FrescutTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Delivery Address Details",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
            )
        }

        HorizontalDivider(color = FrescutBorderLight, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (addresses.isNotEmpty()) {
                Text(
                    text = "Saved Addresses",
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
                addresses.forEach { addr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .frescutOutlineContainer(isSelected = addr.isSelected)
                            .clickable {
                                onSelectAddress(addr.id)
                                onClose()
                            }
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = addr.fullAddress,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (addr.isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = FrescutFocusRed
                            )
                        }
                    }
                }
                HorizontalDivider(color = FrescutBorderLight, thickness = 1.dp)
            }

            Text(
                text = "Add New Address",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )

            // "Use current location" button (requests runtime location permissions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FrescutOutlineShape)
                    .background(FrescutSelectedPinkBg)
                    .border(1.dp, FrescutFocusRed, FrescutOutlineShape)
                    .clickable(enabled = !isFetchingLocation) {
                        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            fetchCurrentDeviceCoordinates()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("use_current_location_btn"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Use current location",
                    tint = FrescutFocusRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFetchingLocation) "Fetching current location..." else "Use current location",
                    color = FrescutFocusRed,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp
                    )
                )
            }

            // Interactive Map Pin Selector (drag pin to adjust coordinates + Confirm location)
            if (isPinSelectorOpen) {
                InteractiveMapPinCard(
                    latitude = pinLat,
                    longitude = pinLng,
                    isReverseGeocoding = isReverseGeocoding,
                    onPinChanged = { newLat, newLng ->
                        pinLat = newLat
                        pinLng = newLng
                    },
                    onConfirmLocation = {
                        confirmPinAndReverseGeocode(pinLat, pinLng)
                    },
                    onDismiss = { isPinSelectorOpen = false }
                )
            }

            if (!locationStatusMessage.isNullOrBlank()) {
                Text(
                    text = locationStatusMessage.orEmpty(),
                    color = if (locationStatusMessage!!.contains("deny", ignoreCase = true)) {
                        FrescutFocusRed
                    } else {
                        FrescutGreenDiscount
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.testTag("location_status_message")
                )
            }

            FrescutLabeledInputField(
                label = "House number / Name / Area *",
                value = houseAndArea,
                onValueChange = { houseAndArea = it },
                placeholder = "e.g., Flat 402, Sunshine Apts, Indiranagar",
                isError = isHouseAreaError,
                testTag = "address_house_area_input"
            )

            FrescutLabeledInputField(
                label = "Floor",
                value = floor,
                onValueChange = { floor = it },
                placeholder = "e.g., 4th Floor (optional)",
                testTag = "address_floor_input"
            )

            FrescutLabeledInputField(
                label = "Landmark",
                value = landmark,
                onValueChange = { landmark = it },
                placeholder = "e.g., Near Metro Station (optional)",
                testTag = "address_landmark_input"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Save as",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Other").forEach { tag ->
                        FrescutOutlineChip(
                            label = tag,
                            isSelected = saveAsLabel.equals(tag, ignoreCase = true),
                            onClick = { saveAsLabel = tag },
                            testTag = "address_chip_${tag.lowercase()}"
                        )
                    }
                }
            }

            FrescutLabeledInputField(
                label = "Receiver's phone number *",
                value = receiverPhone,
                onValueChange = { receiverPhone = it },
                placeholder = "10-digit mobile number",
                isError = isPhoneError,
                testTag = "address_phone_input"
            )

            if (isHouseAreaError || isPhoneError) {
                Text(
                    text = "Please fill in the required fields highlighted in red.",
                    color = FrescutFocusRed,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FrescutPrimaryButton(
                text = "Save Address Details",
                onClick = {
                    attemptedSubmit = true
                    if (houseAndArea.isNotBlank() && receiverPhone.isNotBlank()) {
                        onSaveAddressDetails(
                            saveAsLabel,
                            houseAndArea,
                            floor,
                            landmark,
                            receiverPhone,
                            selectedLat,
                            selectedLng
                        )
                    }
                },
                testTag = "save_address_details_btn"
            )
        }
    }
}

@Composable
private fun InteractiveMapPinCard(
    latitude: Double,
    longitude: Double,
    isReverseGeocoding: Boolean,
    onPinChanged: (Double, Double) -> Unit,
    onConfirmLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasMapsApiKey: Boolean = remember {
        runCatching {
            val clazz = Class.forName("com.example.BuildConfig")
            val field = clazz.getDeclaredField("MAPS_API_KEY")
            val keyVal = field.get(null) as? String
            keyVal != null && keyVal.isNotBlank() && keyVal != "MY_MAPS_API_KEY"
        }.getOrDefault(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frescutOutlineContainer(isSelected = false)
            .padding(12.dp)
            .testTag("interactive_map_pin_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adjust Delivery Pin on Map",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close map",
                    tint = FrescutTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Draggable map canvas where dragging moves the pin's latitude & longitude
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(FrescutOutlineShape)
                .background(Color(0xFFEEF2F6))
                .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                .pointerInput(latitude, longitude) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val nextLat = (latitude - dragAmount.y * 0.00004).coerceIn(-85.0, 85.0)
                        val nextLng = (longitude + dragAmount.x * 0.00004).coerceIn(-180.0, 180.0)
                        onPinChanged(nextLat, nextLng)
                    }
                }
                .testTag("draggable_map_surface"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle street grid lines
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color(0xFFDCE3EB),
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 1.5f
                    )
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color(0xFFDCE3EB),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                    y += step
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Delivery Pin",
                    tint = FrescutFocusRed,
                    modifier = Modifier.size(38.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FrescutWhite)
                        .border(1.dp, FrescutBorderLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%.5f, %.5f", latitude, longitude),
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Text(
            text = if (hasMapsApiKey) {
                "Drag on the map to fine-tune your exact doorstep pin, then tap Confirm location."
            } else {
                "Drag to adjust pin coordinates (Set MAPS_API_KEY in AI Studio Secrets for live Google Map tiles)."
            },
            color = FrescutTextSecondary,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
        )

        FrescutPrimaryButton(
            text = if (isReverseGeocoding) "Fetching address..." else "Confirm location",
            onClick = onConfirmLocation,
            enabled = !isReverseGeocoding,
            testTag = "confirm_location_btn"
        )
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
                    .clip(FrescutOutlineShape),
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
            FrescutPrimaryButton(
                text = "Add ${recipe.cutName} • ₹${recipe.cutPrice}",
                onClick = {
                    onAddPrimaryCut()
                    onDismiss()
                }
            )
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
                        text = "Order Tracking • ${order.orderId}",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Status: ${order.status}",
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (!order.etaText.isNullOrBlank()) {
                Text(
                    text = "ETA: ${order.etaText}",
                    color = FrescutGreenDiscount,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (!order.deliveryPartner.isNullOrBlank() || !order.chillerTemp.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FrescutOutlineShape)
                        .background(FrescutGreenBg)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalShipping,
                        contentDescription = null,
                        tint = FrescutGreenDiscount,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (!order.deliveryPartner.isNullOrBlank()) {
                            Text(
                                text = "Delivery Partner: ${order.deliveryPartner}",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (!order.chillerTemp.isNullOrBlank()) {
                            Text(
                                text = "Chiller Temperature: ${order.chillerTemp}",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            if (!order.route.isNullOrBlank()) {
                Text(
                    text = "Route: ${order.route}",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (order.address.isNotBlank()) {
                Text(
                    text = "Delivery Address: ${order.address}",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    addresses: List<AddressEntity>,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: (String, String, String) -> Unit = { _, _, _ -> },
    onOpenAddressScreen: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var newLabel by remember { mutableStateOf("Home") }
    var newArea by remember { mutableStateOf("") }
    var newFull by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrescutWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Delivery Location",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            if (addresses.isEmpty()) {
                Text(
                    text = "No saved delivery address yet. Please add your delivery address below:",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                addresses.forEach { addr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .frescutOutlineContainer(isSelected = addr.isSelected)
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
                        }
                        if (addr.isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = FrescutFocusRed
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = FrescutBorderLight, thickness = 1.dp)

            Text(
                text = "Add New Delivery Address",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Home", "Work", "Other").forEach { tag ->
                    FrescutOutlineChip(
                        label = tag,
                        isSelected = newLabel.equals(tag, ignoreCase = true),
                        onClick = { newLabel = tag }
                    )
                }
            }
            FrescutLabeledInputField(
                label = "Area / Locality *",
                value = newArea,
                onValueChange = { newArea = it },
                placeholder = "e.g., Indiranagar, Koramangala"
            )
            FrescutLabeledInputField(
                label = "Full Address (House/Flat, Street, Landmark) *",
                value = newFull,
                onValueChange = { newFull = it },
                placeholder = "House no., building name, street",
                singleLine = false
            )
            FrescutPrimaryButton(
                text = "Save Address",
                onClick = { onAddAddress(newLabel, newArea, newFull) },
                enabled = newArea.isNotBlank() && newFull.isNotBlank()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NotificationsScreen(
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("notifications_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("notifications_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FrescutTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Notifications",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )
        }

        HorizontalDivider(color = FrescutDivider)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(FrescutRedLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = FrescutRedBright,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "You're all caught up. Order and offer updates will show here.",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("notifications_empty_message")
                )
            }
        }
    }
}


