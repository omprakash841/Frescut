package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryBudgetConfigEntity
import com.example.data.ProductEntity
import com.example.data.RecommendationConfigEntity
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutGreenBg
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite

@Composable
fun AdminDashboardScreen(
    categoryConfigs: List<CategoryBudgetConfigEntity>,
    recommendationConfig: RecommendationConfigEntity,
    products: List<ProductEntity>,
    onBack: () -> Unit,
    onSaveCategoryConfig: (String, Int, Int) -> Unit,
    onSaveRecommendationConfig: (RecommendationConfigEntity) -> Unit,
    onSaveProductBudgetSettings: (String, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var recEnabled by remember(recommendationConfig) {
        mutableStateOf(recommendationConfig.isEnabled)
    }
    var recTriggerAmount by remember(recommendationConfig) {
        mutableIntStateOf(recommendationConfig.triggerBudgetAmount)
    }
    var recUsePastOrders by remember(recommendationConfig) {
        mutableStateOf(recommendationConfig.usePastOrderHistory)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("admin_dashboard_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("admin_dashboard_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FrescutTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                tint = FrescutRedBright,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Admin Dashboard",
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    )
                )
                Text(
                    text = "Smart Budget Selector & Pricing Controls",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 148.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Category-wise Minimum Budget & Increment Controls
            item(key = "admin_cat_budget_header") {
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
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "1. Category Minimum Order & Increment",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp
                            )
                        )
                        Text(
                            text = "Configure starting budget and step increments per category (e.g., Chicken ₹50, Mutton ₹100, Fish ₹50, Eggs ₹50).",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        categoryConfigs.forEachIndexed { idx, config ->
                            CategoryBudgetConfigRow(
                                config = config,
                                onSave = { minB, inc ->
                                    onSaveCategoryConfig(config.category, minB, inc)
                                }
                            )
                            if (idx < categoryConfigs.lastIndex) {
                                HorizontalDivider(color = FrescutDivider)
                            }
                        }
                    }
                }
            }

            // Section 2: Smart Recommendation Engine Settings
            item(key = "admin_rec_settings") {
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
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "2. Recommendation Generation Settings",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show 'Recommended for You' Suggestions",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Displays 3 dynamic budget options when budget reaches threshold",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = recEnabled,
                                onCheckedChange = {
                                    recEnabled = it
                                    onSaveRecommendationConfig(
                                        recommendationConfig.copy(
                                            isEnabled = it,
                                            triggerBudgetAmount = recTriggerAmount,
                                            usePastOrderHistory = recUsePastOrders
                                        )
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = FrescutWhite, checkedTrackColor = FrescutRedBright)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trigger Threshold Amount",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(FrescutSurface)
                                        .border(1.dp, FrescutBorderLight, CircleShape)
                                        .clickable {
                                            recTriggerAmount = (recTriggerAmount - 10).coerceAtLeast(50)
                                            onSaveRecommendationConfig(
                                                recommendationConfig.copy(
                                                    isEnabled = recEnabled,
                                                    triggerBudgetAmount = recTriggerAmount,
                                                    usePastOrderHistory = recUsePastOrders
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Trigger", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "₹$recTriggerAmount",
                                    color = FrescutRedBright,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(FrescutSurface)
                                        .border(1.dp, FrescutBorderLight, CircleShape)
                                        .clickable {
                                            recTriggerAmount += 10
                                            onSaveRecommendationConfig(
                                                recommendationConfig.copy(
                                                    isEnabled = recEnabled,
                                                    triggerBudgetAmount = recTriggerAmount,
                                                    usePastOrderHistory = recUsePastOrders
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase Trigger", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Include Customer Past Order History",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Personalizes budget picks from real database order history",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = recUsePastOrders,
                                onCheckedChange = {
                                    recUsePastOrders = it
                                    onSaveRecommendationConfig(
                                        recommendationConfig.copy(
                                            isEnabled = recEnabled,
                                            triggerBudgetAmount = recTriggerAmount,
                                            usePastOrderHistory = it
                                        )
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = FrescutWhite, checkedTrackColor = FrescutRedBright)
                            )
                        }
                    }
                }
            }

            // Section 3: Product Price Per KG & Budget Selector Toggle
            item(key = "admin_products_header") {
                Text(
                    text = "3. Product Price per KG & Budget Selector",
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(products, key = { "admin_prod_${it.id}" }) { product ->
                ProductAdminConfigCard(
                    product = product,
                    onSave = { newPricePerKg, enabled ->
                        onSaveProductBudgetSettings(product.id, newPricePerKg, enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetConfigRow(
    config: CategoryBudgetConfigEntity,
    onSave: (Int, Int) -> Unit
) {
    var minBudget by remember(config.minBudget) { mutableIntStateOf(config.minBudget) }
    var increment by remember(config.incrementAmount) { mutableIntStateOf(config.incrementAmount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("admin_cat_row_${config.category.lowercase()}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = config.category,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "Min: ₹$minBudget • Step: +₹$increment",
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Minimum Budget Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Min:",
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(FrescutSurface)
                    .border(1.dp, FrescutBorderLight, CircleShape)
                    .clickable {
                        minBudget = (minBudget - 10).coerceAtLeast(10)
                        onSave(minBudget, increment)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Dec Min", modifier = Modifier.size(14.dp))
            }
            Text(
                text = "₹$minBudget",
                color = FrescutRedBright,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(FrescutSurface)
                    .border(1.dp, FrescutBorderLight, CircleShape)
                    .clickable {
                        minBudget += 10
                        onSave(minBudget, increment)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Inc Min", modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Increment Stepper
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FrescutRedLightBg)
                    .clickable {
                        increment = if (increment == 10) 20 else if (increment == 20) 5 else 10
                        onSave(minBudget, increment)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+₹$increment",
                    color = FrescutRedBright,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun ProductAdminConfigCard(
    product: ProductEntity,
    onSave: (Int, Boolean) -> Unit
) {
    var pricePerKgText by remember(product.effectivePricePerKg) {
        mutableStateOf(product.effectivePricePerKg.toString())
    }
    var budgetEnabled by remember(product.budgetSelectorEnabled) {
        mutableStateOf(product.budgetSelectorEnabled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FrescutWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FrescutBorderLight, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${product.category} • Pack Price: ₹${product.price} (${product.weightInfo})",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (budgetEnabled) "Budget ON" else "Budget OFF",
                        color = if (budgetEnabled) FrescutGreenDiscount else FrescutTextSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = budgetEnabled,
                        onCheckedChange = {
                            budgetEnabled = it
                            val parsed = pricePerKgText.toIntOrNull() ?: product.effectivePricePerKg
                            onSave(parsed, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = FrescutWhite, checkedTrackColor = FrescutRedBright)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = pricePerKgText,
                    onValueChange = { pricePerKgText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Price per KG (₹/kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val parsed = pricePerKgText.toIntOrNull() ?: product.effectivePricePerKg
                        onSave(parsed, budgetEnabled)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = FrescutWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
