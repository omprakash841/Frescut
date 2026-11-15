package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutOutlineBorderWidth
import com.example.ui.theme.FrescutOutlineRadius
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.frescutOutlineContainer

@Composable
fun ReferenceVerticalProductCard(
    product: ProductEntity,
    quantityInCart: Int,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    onDecrementClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 178.dp
) {
    val widthModifier = if (cardWidth != Dp.Unspecified) {
        Modifier.width(cardWidth)
    } else {
        Modifier.fillMaxWidth()
    }
    Card(
        modifier = modifier
            .then(widthModifier)
            .frescutOutlineContainer()
            .clickable(onClick = onCardClick)
            .testTag("product_card_${product.id}"),
        shape = FrescutOutlineShape,
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image (Heart icon removed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
                    .clip(RoundedCornerShape(topStart = FrescutOutlineRadius, topEnd = FrescutOutlineRadius))
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Weight & Serving Info
                Text(
                    text = product.weightAndServings,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price + Discount + Add Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "₹${product.price}",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        )
                        if (product.originalPrice > product.price) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "₹${product.originalPrice}",
                                color = FrescutTextMuted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                        }
                        if (product.discountPercent > 0) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${product.discountPercent}% OFF",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    ProductAddButtonOrStepper(
                        productId = product.id,
                        quantity = quantityInCart,
                        onAddClick = onAddClick,
                        onDecrementClick = onDecrementClick
                    )
                }

                // "30 min delivery" subtle indicator
                if (product.isFlashDelivery) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.deliveryTimeMins} min delivery",
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceHorizontalProductCard(
    product: ProductEntity,
    quantityInCart: Int,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    onDecrementClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .frescutOutlineContainer()
            .clickable(onClick = onCardClick)
            .testTag("cat_product_card_${product.id}"),
        shape = FrescutOutlineShape,
        colors = CardDefaults.cardColors(containerColor = FrescutWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Large Product Photo
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .width(98.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Product Info + Right Add Button (Heart icon removed)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = product.name,
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = product.weightAndServings,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.price}",
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        )
                        if (product.originalPrice > product.price) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${product.originalPrice}",
                                color = FrescutTextMuted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                        }
                        if (product.discountPercent > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${product.discountPercent}% OFF",
                                color = FrescutGreenDiscount,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    ProductAddButtonOrStepper(
                        productId = product.id,
                        quantity = quantityInCart,
                        onAddClick = onAddClick,
                        onDecrementClick = onDecrementClick
                    )
                }
            }
        }
    }
}

@Composable
fun ProductAddButtonOrStepper(
    productId: String,
    quantity: Int,
    onAddClick: () -> Unit,
    onDecrementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (quantity <= 0) {
        // "+ Add" white rounded button with 1dp border & very light shadow (1.5dp) as requested
        Surface(
            modifier = modifier
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = FrescutOutlineBorderWidth,
                    color = FrescutFocusRed,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onAddClick)
                .testTag("add_btn_$productId"),
            shape = RoundedCornerShape(8.dp),
            color = FrescutWhite,
            shadowElevation = 1.5.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Add",
                    color = FrescutFocusRed,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    } else {
        // Active Red Stepper (- 1 +)
        Row(
            modifier = modifier
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FrescutFocusRed)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDecrementClick)
                    .testTag("dec_btn_$productId"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease quantity",
                    tint = FrescutWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = quantity.toString(),
                color = FrescutWhite,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddClick)
                    .testTag("inc_btn_$productId"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase quantity",
                    tint = FrescutWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
