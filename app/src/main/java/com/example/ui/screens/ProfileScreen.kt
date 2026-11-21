package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AddressEntity
import com.example.data.AppConfigModel
import com.example.data.CouponOfferModel
import com.example.data.ProfileSubpage
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutLabeledInputField
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutPrimaryButton
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedPrimary
import com.example.ui.theme.FrescutSectionDivider
import com.example.ui.theme.FrescutSelectedPinkBg
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutThinDivider
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.frescutOutlineContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userPhone: String,
    userEmail: String,
    isProMember: Boolean = false,
    selectedPaymentMethod: String,
    addresses: List<AddressEntity>,
    activeCoupons: List<CouponOfferModel>,
    referralCode: String = "",
    activeSubpage: com.example.ui.ProfileSubpage?,
    onOpenSubpage: (com.example.ui.ProfileSubpage?) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: (String, String, String) -> Unit = { _, _, _ -> },
    onTogglePro: () -> Unit = {},
    onSelectPaymentMethod: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onLogoutConfirmed: () -> Unit,
    onNavigateToOrdersTab: () -> Unit = {},
    onOpenAddressScreen: () -> Unit = {},
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isLogoutConfirmDialogOpen by remember { mutableStateOf(false) }

    val mappedSubpage = when (activeSubpage) {
        com.example.ui.ProfileSubpage.MY_ADDRESSES -> ProfileSubpage.ADDRESSES
        com.example.ui.ProfileSubpage.PAYMENT_METHODS -> ProfileSubpage.PAYMENTS
        com.example.ui.ProfileSubpage.COUPONS_AND_OFFERS -> ProfileSubpage.COUPONS
        com.example.ui.ProfileSubpage.REFER_AND_EARN -> ProfileSubpage.REFER_EARN
        com.example.ui.ProfileSubpage.HELP_AND_SUPPORT -> ProfileSubpage.HELP_SUPPORT
        com.example.ui.ProfileSubpage.ABOUT_FRESCUT -> ProfileSubpage.ABOUT_US
        com.example.ui.ProfileSubpage.FRESCUT_PRO, null -> ProfileSubpage.NONE
    }

    ProfileScreen(
        userName = userName,
        userPhone = userPhone,
        userEmail = userEmail,
        isGuestUser = userEmail.isBlank() || userEmail.equals("guest@frescut.in", ignoreCase = true),
        addresses = addresses,
        selectedPaymentMethod = selectedPaymentMethod,
        activeCoupons = activeCoupons,
        appConfig = AppConfigModel(referralCode = referralCode),
        activeSubpage = mappedSubpage,
        isEditProfileDialogOpen = isEditProfileDialogOpen,
        isLanguageDialogOpen = isLanguageDialogOpen,
        isLogoutConfirmDialogOpen = isLogoutConfirmDialogOpen,
        onOpenSubpage = { next ->
            val uiSubpage = when (next) {
                ProfileSubpage.ADDRESSES -> com.example.ui.ProfileSubpage.MY_ADDRESSES
                ProfileSubpage.PAYMENTS -> com.example.ui.ProfileSubpage.PAYMENT_METHODS
                ProfileSubpage.COUPONS -> com.example.ui.ProfileSubpage.COUPONS_AND_OFFERS
                ProfileSubpage.REFER_EARN -> com.example.ui.ProfileSubpage.REFER_AND_EARN
                ProfileSubpage.HELP_SUPPORT -> com.example.ui.ProfileSubpage.HELP_AND_SUPPORT
                ProfileSubpage.ABOUT_US -> com.example.ui.ProfileSubpage.ABOUT_FRESCUT
                ProfileSubpage.NONE -> null
            }
            onOpenSubpage(uiSubpage)
        },
        onNavigateToOrdersTab = onNavigateToOrdersTab,
        onOpenAddressScreen = onOpenAddressScreen,
        onSelectAddress = { onSelectAddress(it.id) },
        onSelectPaymentMethod = onSelectPaymentMethod,
        onApplyCouponFromProfile = onApplyCoupon,
        onSetEditProfileDialogOpen = { isEditProfileDialogOpen = it },
        onSetLanguageDialogOpen = { isLanguageDialogOpen = it },
        onSetLogoutConfirmDialogOpen = { isLogoutConfirmDialogOpen = it },
        onUpdateProfile = onUpdateProfile,
        onSignOutClick = onLogoutConfirmed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userPhone: String,
    userEmail: String,
    isGuestUser: Boolean,
    addresses: List<AddressEntity>,
    selectedPaymentMethod: String,
    activeCoupons: List<CouponOfferModel>,
    appConfig: AppConfigModel,
    activeSubpage: ProfileSubpage,
    isEditProfileDialogOpen: Boolean,
    isLanguageDialogOpen: Boolean,
    isLogoutConfirmDialogOpen: Boolean,
    onOpenSubpage: (ProfileSubpage) -> Unit,
    onNavigateToOrdersTab: () -> Unit,
    onOpenAddressScreen: () -> Unit,
    onSelectAddress: (AddressEntity) -> Unit,
    onSelectPaymentMethod: (String) -> Unit,
    onApplyCouponFromProfile: (String) -> Unit,
    onSetEditProfileDialogOpen: (Boolean) -> Unit,
    onSetLanguageDialogOpen: (Boolean) -> Unit,
    onSetLogoutConfirmDialogOpen: (Boolean) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeSubpage != ProfileSubpage.NONE) {
        BackHandler { onOpenSubpage(ProfileSubpage.NONE) }
        ProfileSubpageView(
            subpage = activeSubpage,
            addresses = addresses,
            selectedPaymentMethod = selectedPaymentMethod,
            activeCoupons = activeCoupons,
            appConfig = appConfig,
            onBack = { onOpenSubpage(ProfileSubpage.NONE) },
            onSelectAddress = onSelectAddress,
            onOpenAddressScreen = onOpenAddressScreen,
            onSelectPaymentMethod = onSelectPaymentMethod,
            onApplyCoupon = onApplyCouponFromProfile
        )
        return
    }

    val savedAddressesSubtitle = if (addresses.isEmpty()) {
        stringResource(R.string.profile_no_saved_address)
    } else {
        stringResource(R.string.profile_addresses_saved_format, addresses.size)
    }

    val displayUserName = if (isGuestUser || userName.isBlank()) {
        stringResource(R.string.profile_guest_name)
    } else {
        userName
    }
    val displayEmail = if (isGuestUser || userEmail.equals("guest@frescut.in", ignoreCase = true)) {
        ""
    } else {
        userEmail
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header Title
        item(key = "profile_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
            }
        }

        // User Identity Section
        item(key = "profile_user_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(FrescutRedLightBg),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = displayUserName
                            .trim()
                            .split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                            .ifEmpty { "C" }
                        Text(
                            text = initials,
                            color = FrescutRedPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayUserName,
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("profile_user_name")
                        )
                        if (userPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userPhone,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp)
                            )
                        }
                        if (displayEmail.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayEmail,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!isGuestUser) {
                    IconButton(
                        onClick = { onSetEditProfileDialogOpen(true) },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.profile_edit),
                            tint = FrescutRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item(key = "profile_section_divider") {
            FrescutSectionDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Profile rows without outer box border, separated only by thin lines, in exact order:
        // 1. My Addresses
        // 2. Orders (navigates to Orders tab)
        // 3. Payment Methods (Cash on Delivery)
        // 4. Language (English)
        // 5. Coupons & Offers (only if activeCoupons is not empty)
        // (Refer & Earn only if appConfig.referralCode is not blank)
        // 6. Help & Support
        // 7. About (only if appConfig.aboutText is not blank)
        // 8. Logout
        item(key = "profile_rows_list") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FrescutWhite)
            ) {
                // 1. My Addresses
                ProfileMenuRow(
                    icon = Icons.Filled.LocationOn,
                    title = stringResource(R.string.profile_my_addresses),
                    subtitle = savedAddressesSubtitle,
                    onClick = onOpenAddressScreen,
                    testTag = "profile_row_addresses"
                )
                FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 2. Orders (navigates to Orders tab)
                ProfileMenuRow(
                    icon = Icons.Filled.ReceiptLong,
                    title = stringResource(R.string.profile_orders),
                    subtitle = stringResource(R.string.profile_orders_subtitle),
                    onClick = onNavigateToOrdersTab,
                    testTag = "profile_row_orders"
                )
                FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 3. Payment Methods (Cash on Delivery)
                ProfileMenuRow(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = stringResource(R.string.profile_payment_methods),
                    subtitle = stringResource(R.string.profile_cod_subtitle),
                    onClick = { onOpenSubpage(ProfileSubpage.PAYMENTS) },
                    testTag = "profile_row_payments"
                )
                FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 4. Language (English)
                ProfileMenuRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.profile_language),
                    subtitle = stringResource(R.string.profile_language_english),
                    onClick = { onSetLanguageDialogOpen(true) },
                    testTag = "profile_row_language"
                )
                FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 5. Coupons & Offers (only when at least 1 active coupon exists)
                if (activeCoupons.isNotEmpty()) {
                    ProfileMenuRow(
                        icon = Icons.Filled.LocalOffer,
                        title = stringResource(R.string.profile_coupons_offers),
                        subtitle = "${activeCoupons.size} active",
                        onClick = { onOpenSubpage(ProfileSubpage.COUPONS) },
                        testTag = "profile_row_coupons"
                    )
                    FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Refer & Earn (only when config.referralCode is non-blank)
                if (appConfig.referralCode.isNotBlank()) {
                    ProfileMenuRow(
                        icon = Icons.Filled.CardGiftcard,
                        title = stringResource(R.string.profile_refer_earn),
                        subtitle = appConfig.referralCode,
                        onClick = { onOpenSubpage(ProfileSubpage.REFER_EARN) },
                        testTag = "profile_row_refer_earn"
                    )
                    FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // 6. Help & Support
                ProfileMenuRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(R.string.profile_help_support),
                    subtitle = appConfig.supportPhone.takeIf { it.isNotBlank() },
                    onClick = { onOpenSubpage(ProfileSubpage.HELP_SUPPORT) },
                    testTag = "profile_row_help_support"
                )
                FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 7. About (only when config.aboutText is non-blank)
                if (appConfig.aboutText.isNotBlank()) {
                    ProfileMenuRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.profile_about),
                        subtitle = appConfig.storeName,
                        onClick = { onOpenSubpage(ProfileSubpage.ABOUT_US) },
                        testTag = "profile_row_about"
                    )
                    FrescutThinDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // 8. Logout
                ProfileMenuRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = stringResource(R.string.profile_logout),
                    subtitle = null,
                    isDestructive = true,
                    onClick = { onSetLogoutConfirmDialogOpen(true) },
                    testTag = "profile_row_logout"
                )
            }
        }
    }

    // Language Bottom Sheet (English selected + "More languages coming soon" below)
    if (isLanguageDialogOpen) {
        ModalBottomSheet(
            onDismissRequest = { onSetLanguageDialogOpen(false) },
            containerColor = FrescutWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("language_bottom_sheet"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_language),
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frescutOutlineContainer(isSelected = true)
                        .clickable { onSetLanguageDialogOpen(false) }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .testTag("language_option_english"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.profile_language_english),
                        color = FrescutFocusRed,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.profile_language_english),
                        tint = FrescutFocusRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.profile_language_coming_soon),
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 12.dp)
                        .testTag("language_coming_soon_text")
                )
            }
        }
    }

    // Logout Confirmation Dialog ("Log out of FRESCUT?" — "Cancel" and "Log out")
    if (isLogoutConfirmDialogOpen) {
        AlertDialog(
            onDismissRequest = { onSetLogoutConfirmDialogOpen(false) },
            containerColor = FrescutWhite,
            title = {
                Text(
                    text = stringResource(R.string.logout_confirm_title),
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetLogoutConfirmDialogOpen(false)
                        onSignOutClick()
                    },
                    modifier = Modifier.testTag("logout_confirm_btn")
                ) {
                    Text(
                        text = stringResource(R.string.logout_confirm_action),
                        color = FrescutFocusRed,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onSetLogoutConfirmDialogOpen(false) },
                    modifier = Modifier.testTag("logout_cancel_btn")
                ) {
                    Text(
                        text = stringResource(R.string.logout_confirm_cancel),
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        )
    }

    // Edit Profile Dialog
    if (isEditProfileDialogOpen) {
        var editName by remember(userName) { mutableStateOf(userName) }
        var editPhone by remember(userPhone) { mutableStateOf(userPhone) }
        var editEmail by remember(displayEmail) { mutableStateOf(displayEmail) }
        var nameError by remember { mutableStateOf(false) }
        var phoneError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { onSetEditProfileDialogOpen(false) },
            containerColor = FrescutWhite,
            title = {
                Text(
                    text = stringResource(R.string.profile_edit),
                    color = FrescutTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FrescutLabeledInputField(
                        label = "Name *",
                        value = editName,
                        onValueChange = {
                            editName = it
                            nameError = false
                        },
                        placeholder = "Your name",
                        isError = nameError,
                        errorMessage = if (nameError) "Please enter your name" else null
                    )
                    FrescutLabeledInputField(
                        label = "Phone",
                        value = editPhone,
                        onValueChange = {
                            editPhone = it
                            phoneError = false
                        },
                        placeholder = "10-digit mobile number",
                        isError = phoneError,
                        errorMessage = if (phoneError) "Enter a valid 10-digit phone number" else null
                    )
                    FrescutLabeledInputField(
                        label = "Email",
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        placeholder = "Email address"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedPhone = editPhone.trim()
                        val phoneDigits = trimmedPhone.filter { it.isDigit() }.let { d ->
                            if (d.length > 10) d.takeLast(10) else d
                        }
                        val isPhoneInvalid = trimmedPhone.isNotEmpty() &&
                            (phoneDigits.length != 10 || phoneDigits.first() !in listOf('6', '7', '8', '9'))
                        if (editName.isBlank()) {
                            nameError = true
                        } else if (isPhoneInvalid) {
                            phoneError = true
                        } else {
                            onUpdateProfile(editName, if (trimmedPhone.isEmpty()) "" else phoneDigits, editEmail)
                            onSetEditProfileDialogOpen(false)
                        }
                    }
                ) {
                    Text(
                        text = "Save",
                        color = FrescutFocusRed,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onSetEditProfileDialogOpen(false) }) {
                    Text(
                        text = stringResource(R.string.logout_confirm_cancel),
                        color = FrescutTextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    testTag: String
) {
    val contentColor = if (isDestructive) FrescutFocusRed else FrescutTextPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) FrescutFocusRed else FrescutRedBright,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = FrescutTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileSubpageView(
    subpage: ProfileSubpage,
    addresses: List<AddressEntity>,
    selectedPaymentMethod: String,
    activeCoupons: List<CouponOfferModel>,
    appConfig: AppConfigModel,
    onBack: () -> Unit,
    onSelectAddress: (AddressEntity) -> Unit,
    onOpenAddressScreen: () -> Unit,
    onSelectPaymentMethod: (String) -> Unit,
    onApplyCoupon: (String) -> Unit
) {
    val context = LocalContext.current
    val title = when (subpage) {
        ProfileSubpage.ADDRESSES -> stringResource(R.string.profile_my_addresses)
        ProfileSubpage.PAYMENTS -> stringResource(R.string.profile_payment_methods)
        ProfileSubpage.COUPONS -> stringResource(R.string.profile_coupons_offers)
        ProfileSubpage.REFER_EARN -> stringResource(R.string.profile_refer_earn)
        ProfileSubpage.HELP_SUPPORT -> stringResource(R.string.profile_help_support)
        ProfileSubpage.ABOUT_US -> stringResource(R.string.profile_about)
        ProfileSubpage.NONE -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("profile_subpage_${subpage.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("profile_subpage_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FrescutTextPrimary
                )
            }
            Text(
                text = title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        FrescutThinDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (subpage) {
                ProfileSubpage.PAYMENTS -> {
                    // Selectable payment option with 1dp border (#EEEEEE or red when selected)
                    val codLabel = stringResource(R.string.profile_cod_subtitle)
                    val isSelected = selectedPaymentMethod.contains("Cash", ignoreCase = true) ||
                        selectedPaymentMethod.equals(codLabel, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .frescutOutlineContainer(isSelected = isSelected)
                            .clickable { onSelectPaymentMethod(codLabel) }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .testTag("payment_option_cod"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = codLabel,
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pay at your doorstep when your order arrives",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = FrescutFocusRed
                            )
                        }
                    }
                }

                ProfileSubpage.COUPONS -> {
                    activeCoupons.forEach { coupon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FrescutOutlineShape)
                                .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = coupon.code,
                                    color = FrescutRedPrimary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = coupon.title,
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (coupon.description.isNotBlank()) {
                                    Text(
                                        text = coupon.description,
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            TextButton(onClick = { onApplyCoupon(coupon.code) }) {
                                Text(
                                    text = stringResource(R.string.cart_apply_btn),
                                    color = FrescutFocusRed,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }

                ProfileSubpage.REFER_EARN -> {
                    if (appConfig.referralCode.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FrescutOutlineShape)
                                .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Your Referral Code",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = appConfig.referralCode,
                                color = FrescutRedPrimary,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }

                ProfileSubpage.HELP_SUPPORT -> {
                    // KAAM 11.2: Call and WhatsApp buttons powered by config.supportPhone; hidden if supportPhone is blank
                    Text(
                        text = "Need help with an order or delivery?",
                        color = FrescutTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Our support team is happy to assist you with order updates, delivery timing, or billing queries.",
                        color = FrescutTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (appConfig.supportPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val cleanDigits = appConfig.supportPhone.filter { it.isDigit() || it == '+' }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("help_support_contact_buttons"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FrescutPrimaryButton(
                                text = stringResource(R.string.profile_help_call),
                                onClick = {
                                    runCatching {
                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanDigits"))
                                        context.startActivity(callIntent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "help_call_btn"
                            )

                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        val waDigits = appConfig.supportPhone.filter { it.isDigit() }
                                        val waIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://wa.me/$waDigits")
                                        )
                                        context.startActivity(waIntent)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("help_whatsapp_btn"),
                                shape = FrescutOutlineShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FrescutGreenDiscount)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Chat,
                                    contentDescription = null,
                                    tint = FrescutGreenDiscount,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.profile_help_whatsapp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }

                ProfileSubpage.ABOUT_US -> {
                    // KAAM 6.2 & 11.3: Show storeName, aboutText, and fssaiNumber ("FSSAI Lic. No. <number>" if non-blank)
                    Text(
                        text = appConfig.storeName,
                        color = FrescutRedPrimary,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.testTag("about_store_name")
                    )
                    if (appConfig.aboutText.isNotBlank()) {
                        Text(
                            text = appConfig.aboutText,
                            color = FrescutTextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.testTag("about_text_body")
                        )
                    }
                    if (appConfig.fssaiNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.profile_fssai_format, appConfig.fssaiNumber),
                            color = FrescutTextSecondary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier.testTag("about_fssai_number")
                        )
                    }
                }

                ProfileSubpage.ADDRESSES -> {
                    addresses.forEach { addr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .frescutOutlineContainer(isSelected = addr.isSelected)
                                .clickable { onSelectAddress(addr) }
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
                    FrescutPrimaryButton(
                        text = stringResource(R.string.cart_add_address_btn),
                        onClick = onOpenAddressScreen
                    )
                }

                ProfileSubpage.NONE -> Unit
            }
        }
    }
}
