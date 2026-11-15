package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FrescutOutlineRadius: Dp = 12.dp
val FrescutOutlineBorderWidth: Dp = 1.dp
val FrescutOutlineShape: Shape = RoundedCornerShape(12.dp)

/**
 * Centralized FRESCUT outline card/container style:
 * - Unselected: 1dp #E0E0E0 border, 12dp corner radius, white (#FFFFFF) background
 * - Selected: 1dp #C8101E red border, 12dp corner radius, light pink (#FFF0F1) background
 * - Error: 1dp #C8101E red border, 12dp corner radius, white (#FFFFFF) background
 */
fun Modifier.frescutOutlineContainer(
    isSelected: Boolean = false,
    isError: Boolean = false,
    shape: Shape = FrescutOutlineShape,
    backgroundColor: Color = if (isSelected) FrescutSelectedPinkBg else FrescutWhite
): Modifier {
    val borderColor = when {
        isSelected || isError -> FrescutFocusRed
        else -> FrescutBorderLight
    }
    return this
        .clip(shape)
        .background(backgroundColor, shape)
        .border(width = FrescutOutlineBorderWidth, color = borderColor, shape = shape)
}

/**
 * Centralized labeled input field with small grey label above and 1dp #E0E0E0 bordered 12dp white box.
 * Border turns red (#C8101E) when focused or when [isError] is true.
 */
@Composable
fun FrescutLabeledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused || isError) FrescutFocusRed else FrescutBorderLight

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            color = if (isError) FrescutFocusRed else FrescutTextSecondary,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FrescutOutlineShape)
                .background(FrescutWhite, FrescutOutlineShape)
                .border(FrescutOutlineBorderWidth, borderColor, FrescutOutlineShape)
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = FrescutTextMuted,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                interactionSource = interactionSource,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(FrescutFocusRed),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = FrescutTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            )
        }
    }
}

/**
 * Small outline chip (e.g. Home / Work / Other):
 * - Unselected: white background, 1dp #E0E0E0 border, 12dp radius
 * - Selected: light pink (#FFF0F1) background, 1dp #C8101E red border, 12dp radius
 */
@Composable
fun FrescutOutlineChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .frescutOutlineContainer(isSelected = isSelected)
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) FrescutFocusRed else FrescutTextPrimary,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 12.5.sp
            )
        )
    }
}

/**
 * Separate bordered card for each payment method option with a custom radio button on the right.
 * Selected option uses 1dp red border (#C8101E) and light pink background (#FFF0F1).
 */
@Composable
fun FrescutPaymentOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .frescutOutlineContainer(isSelected = isSelected)
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) FrescutFocusRed else FrescutTextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp)
                )
            }
        }

        // Radio Button on the right
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.5.dp,
                    color = if (isSelected) FrescutFocusRed else FrescutBorderLight,
                    shape = CircleShape
                )
                .background(FrescutWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FrescutFocusRed, CircleShape)
                )
            }
        }
    }
}

/**
 * Primary action button (Place Order, Add to Cart, Save Address Details):
 * - No border, full red filled, 12dp rounded corners, no shadow.
 */
@Composable
fun FrescutPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = FrescutOutlineShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = FrescutFocusRed,
            contentColor = FrescutWhite,
            disabledContainerColor = FrescutBorderLight,
            disabledContentColor = FrescutTextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier
            .height(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            color = if (enabled) FrescutWhite else FrescutTextSecondary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.5.sp
            )
        )
    }
}

/**
 * Common 1dp light grey (#E0E0E0) horizontal divider used across the entire app:
 * - Below search bar & category tabs on Home and Categories
 * - Above "Amount to be paid" in Bill Summary
 * - Between Profile menu items (with startIndent after the icon)
 * - Inside My Orders cards between the 3 sections
 * - At the top of the bottom navigation bar
 */
@Composable
fun FrescutThinDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    thickness: Dp = 1.dp,
    color: Color = FrescutBorderLight
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startIndent, end = endIndent)
            .height(thickness)
            .background(color)
    )
}

/**
 * Common 8dp thick light grey (#F5F5F5) section divider band used between major sections
 * in Cart & Checkout (Items, Coupon, Bill Summary, Policies).
 */
@Composable
fun FrescutSectionBandDivider(
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = FrescutSectionDividerBg
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(color)
    )
}

/**
 * Common 2dp red (#C8101E) underline indicator shown beneath the selected category tab.
 */
@Composable
fun FrescutSelectedTabUnderline(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = FrescutFocusRed
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(if (isSelected) color else Color.Transparent)
    )
}

fun formatItemCount(count: Int, capitalized: Boolean = false): String {
    val word = if (count == 1) {
        if (capitalized) "Item" else "item"
    } else {
        if (capitalized) "Items" else "items"
    }
    return "$count $word"
}

fun formatFreshCutCount(count: Int): String {
    return if (count == 1) "1 fresh cut" else "$count fresh cuts"
}

private val FrescutColorScheme = lightColorScheme(
    primary = FrescutRedBright,
    onPrimary = FrescutWhite,
    primaryContainer = FrescutRedLightBg,
    onPrimaryContainer = FrescutRedDark,
    secondary = FrescutGoldDark,
    onSecondary = FrescutWhite,
    secondaryContainer = FrescutOrangeBg,
    onSecondaryContainer = FrescutTextPrimary,
    tertiary = FrescutGreenDiscount,
    onTertiary = FrescutWhite,
    background = FrescutWhite,
    onBackground = FrescutTextPrimary,
    surface = FrescutWhite,
    onSurface = FrescutTextPrimary,
    surfaceVariant = FrescutSurface,
    onSurfaceVariant = FrescutTextSecondary,
    outline = FrescutBorderLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrescutColorScheme,
        typography = Typography,
        content = content
    )
}
