package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
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
import com.example.ui.theme.FrescutOutlineRadius
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.rememberAnimationsEnabled

@Composable
fun ReferenceVerticalProductCard(
    product: ProductEntity,
    quantityInCart: Int,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    onDecrementClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 172.dp
) {
    UnifiedFrescutProductCard(
        product = product,
        quantityInCart = quantityInCart,
        onCardClick = onCardClick,
        onAddClick = onAddClick,
        onDecrementClick = onDecrementClick,
        testTag = "product_card_${product.id}",
        cardWidth = cardWidth,
        modifier = modifier
    )
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
    UnifiedFrescutProductCard(
        product = product,
        quantityInCart = quantityInCart,
        onCardClick = onCardClick,
        onAddClick = onAddClick,
        onDecrementClick = onDecrementClick,
        testTag = "cat_product_card_${product.id}",
        cardWidth = Dp.Unspecified,
        modifier = modifier
    )
}

@Composable
private fun UnifiedFrescutProductCard(
    product: ProductEntity,
    quantityInCart: Int,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    onDecrementClick: () -> Unit,
    testTag: String,
    cardWidth: Dp,
    modifier: Modifier = Modifier
) {
    val widthModifier = if (cardWidth != Dp.Unspecified) {
        Modifier.width(cardWidth)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier
            .then(widthModifier)
            .background(FrescutWhite)
            .clickable(onClick = onCardClick)
            .testTag(testTag)
    ) {
        // 1 & 2. Top Photo Box (1dp #E0E0E0 border, 12dp rounded corners, only photo inside)
        // Add/Stepper button sits at bottom-right: half on photo, half below photo border, and 6dp outside right edge of photo
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp)
                    .height(114.dp)
                    .clip(FrescutOutlineShape)
                    .background(FrescutWhite, FrescutOutlineShape)
                    .border(
                        width = 1.dp,
                        color = FrescutBorderLight,
                        shape = FrescutOutlineShape
                    )
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(FrescutOutlineRadius)),
                    contentScale = ContentScale.Crop
                )
            }

            ProductAddButtonOrStepper(
                productId = product.id,
                quantity = quantityInCart,
                onAddClick = onAddClick,
                onDecrementClick = onDecrementClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 16.dp)
            )
        }

        // 3. 20dp space below photo for bottom half of the Add/Stepper button
        Spacer(modifier = Modifier.height(20.dp))

        // Product Name (bold, 1 line, ellipsis if long)
        Text(
            text = product.name,
            color = FrescutTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                lineHeight = 17.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 6.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Weight (grey, 1 line)
        Text(
            text = product.weightInfo.ifBlank { product.piecesOrServes },
            color = FrescutTextSecondary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Price Row: Price (bold), MRP (strikethrough, grey), Discount % (green) — separate line below weight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "₹${product.price}",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )
            if (product.originalPrice > product.price) {
                Text(
                    text = "₹${product.originalPrice}",
                    color = FrescutTextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        textDecoration = TextDecoration.LineThrough
                    ),
                    maxLines = 1
                )
            }
            if (product.discountPercent > 0) {
                Text(
                    text = "${product.discountPercent}% OFF",
                    color = FrescutGreenDiscount,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1
                )
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
    val animationsEnabled = rememberAnimationsEnabled()
    val view = LocalView.current
    val buttonShape = RoundedCornerShape(8.dp)

    val addInteractionSource = remember { MutableInteractionSource() }
    val decInteractionSource = remember { MutableInteractionSource() }
    val incInteractionSource = remember { MutableInteractionSource() }

    val isAddPressed by addInteractionSource.collectIsPressedAsState()
    val isDecPressed by decInteractionSource.collectIsPressedAsState()
    val isIncPressed by incInteractionSource.collectIsPressedAsState()

    val isAnyPressed = isAddPressed || isDecPressed || isIncPressed
    val pressScale by animateFloatAsState(
        targetValue = if (isAnyPressed && animationsEnabled) 0.95f else 1.0f,
        animationSpec = tween(
            durationMillis = if (animationsEnabled) 150 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "AddOrStepperPressScale"
    )

    fun triggerLightHaptic() {
        runCatching {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    val isStepperActive = quantity > 0

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .height(32.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = if (animationsEnabled) 200 else 0,
                    easing = FastOutSlowInEasing
                )
            )
            .border(
                width = 1.dp,
                color = FrescutFocusRed,
                shape = buttonShape
            ),
        shape = buttonShape,
        color = FrescutWhite,
        shadowElevation = if (isStepperActive) 2.dp else 0.dp,
        tonalElevation = 0.dp
    ) {
        AnimatedContent(
            targetState = isStepperActive,
            transitionSpec = {
                if (!animationsEnabled) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    fadeIn(
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                    )
                }
            },
            label = "AddStepperStateTransition"
        ) { showStepper ->
            if (!showStepper) {
                // Add Button: ~76dp x 32dp, white background, 1dp red (#C8101E) border, 8dp corners, small bag icon + bold red "Add", no shadow
                Row(
                    modifier = Modifier
                        .width(76.dp)
                        .height(32.dp)
                        .clip(buttonShape)
                        .background(FrescutWhite)
                        .clickable(
                            interactionSource = addInteractionSource,
                            indication = null,
                            onClick = {
                                triggerLightHaptic()
                                onAddClick()
                            }
                        )
                        .testTag("add_btn_$productId"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = FrescutFocusRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        color = FrescutFocusRed,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1
                    )
                }
            } else {
                // Stepper: ~92dp x 32dp, 3 sections: left thin red "−" on white, middle dark grey qty on #F2F2F2, right thin red "+" on white
                Row(
                    modifier = Modifier
                        .width(92.dp)
                        .height(32.dp)
                        .clip(buttonShape)
                        .background(FrescutWhite),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Section: "−"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(FrescutWhite)
                            .clickable(
                                interactionSource = decInteractionSource,
                                indication = null,
                                onClick = {
                                    triggerLightHaptic()
                                    onDecrementClick()
                                }
                            )
                            .testTag("dec_btn_$productId"),
                        contentAlignment = Alignment.Center
                    ) {
                        ThinMinusIcon(color = FrescutFocusRed)
                    }

                    // Middle Section: Quantity in dark grey text on light grey (#F2F2F2) background with vertical slide animation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFF2F2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = quantity.coerceAtLeast(1),
                            transitionSpec = {
                                if (!animationsEnabled) {
                                    EnterTransition.None togetherWith ExitTransition.None
                                } else if (targetState > initialState) {
                                    // "+" pressed: number slides from top to bottom
                                    (slideInVertically(
                                        animationSpec = tween(180, easing = FastOutSlowInEasing),
                                        initialOffsetY = { fullHeight -> -fullHeight }
                                    ) + fadeIn(
                                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                                    )) togetherWith (slideOutVertically(
                                        animationSpec = tween(180, easing = FastOutSlowInEasing),
                                        targetOffsetY = { fullHeight -> fullHeight }
                                    ) + fadeOut(
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    ))
                                } else {
                                    // "−" pressed: number slides from bottom to top
                                    (slideInVertically(
                                        animationSpec = tween(180, easing = FastOutSlowInEasing),
                                        initialOffsetY = { fullHeight -> fullHeight }
                                    ) + fadeIn(
                                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                                    )) togetherWith (slideOutVertically(
                                        animationSpec = tween(180, easing = FastOutSlowInEasing),
                                        targetOffsetY = { fullHeight -> -fullHeight }
                                    ) + fadeOut(
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    ))
                                }
                            },
                            label = "QuantityNumberSlide"
                        ) { targetQty ->
                            Text(
                                text = targetQty.toString(),
                                color = Color(0xFF1F2937),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    // Right Section: "+"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(FrescutWhite)
                            .clickable(
                                interactionSource = incInteractionSource,
                                indication = null,
                                onClick = {
                                    triggerLightHaptic()
                                    onAddClick()
                                }
                            )
                            .testTag("inc_btn_$productId"),
                        contentAlignment = Alignment.Center
                    ) {
                        ThinPlusIcon(color = FrescutFocusRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinMinusIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(11.dp)) {
        val strokePx = 1.6.dp.toPx()
        val cy = size.height / 2f
        drawLine(
            color = color,
            start = Offset(1.dp.toPx(), cy),
            end = Offset(size.width - 1.dp.toPx(), cy),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ThinPlusIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(11.dp)) {
        val strokePx = 1.6.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawLine(
            color = color,
            start = Offset(1.dp.toPx(), cy),
            end = Offset(size.width - 1.dp.toPx(), cy),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(cx, 1.dp.toPx()),
            end = Offset(cx, size.height - 1.dp.toPx()),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}
