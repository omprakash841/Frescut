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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddressEntity
import com.example.data.CouponOfferModel
import com.example.ui.ProfileSubpage
import com.example.ui.components.ObserveSmartBottomNavScroll
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutGreenDiscount
import com.example.ui.theme.FrescutLabeledInputField
import com.example.ui.theme.FrescutOutlineChip
import com.example.ui.theme.FrescutOutlineShape
import com.example.ui.theme.FrescutPaymentOptionCard
import com.example.ui.theme.FrescutPrimaryButton
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutSelectedPinkBg
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutThinDivider
import com.example.ui.theme.FrescutWhite

private data class ProfileMenuRowItem(
    val subpage: ProfileSubpage?,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isLogout: Boolean = false
)

@Composable
fun ProfileScreen(
    userName: String,
    userPhone: String,
    userEmail: String,
    isProMember: Boolean,
    selectedPaymentMethod: String,
    addresses: List<AddressEntity>,
    activeCoupons: List<CouponOfferModel> = emptyList(),
    referralCode: String = "",
    activeSubpage: ProfileSubpage?,
    onOpenSubpage: (ProfileSubpage?) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: (String, String, String) -> Unit,
    onTogglePro: () -> Unit,
    onSelectPaymentMethod: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onLogoutConfirmed: () -> Unit,
    onBottomNavVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    ObserveSmartBottomNavScroll(
        listState = listState,
        onBottomNavVisibilityChanged = onBottomNavVisibilityChanged
    )

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (activeSubpage != null) {
        BackHandler { onOpenSubpage(null) }
        ProfileSubpageView(
            subpage = activeSubpage,
            isProMember = isProMember,
            selectedPaymentMethod = selectedPaymentMethod,
            addresses = addresses,
            activeCoupons = activeCoupons,
            referralCode = referralCode,
            onBack = { onOpenSubpage(null) },
            onSelectAddress = onSelectAddress,
            onAddAddress = onAddAddress,
            onTogglePro = onTogglePro,
            onSelectPaymentMethod = onSelectPaymentMethod,
            onApplyCoupon = onApplyCoupon,
            modifier = modifier
        )
        return
    }

    val menuItems = remember(addresses.size, isProMember, selectedPaymentMethod, activeCoupons.size, referralCode) {
        listOf(
            ProfileMenuRowItem(
                subpage = ProfileSubpage.MY_ADDRESSES,
                title = "My Addresses",
                subtitle = when (addresses.size) {
                    0 -> "No saved delivery addresses"
                    1 -> "1 saved delivery address"
                    else -> "${addresses.size} saved delivery addresses"
                },
                icon = Icons.Outlined.LocationOn
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.PAYMENT_METHODS,
                title = "Payment Methods",
                subtitle = selectedPaymentMethod,
                icon = Icons.Outlined.AccountBalanceWallet
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.FRESCUT_PRO,
                title = "FRESCUT Plus",
                subtitle = if (isProMember) "Active • Free delivery unlocked" else "Join for unlimited free delivery",
                icon = Icons.Outlined.WorkspacePremium
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.REFER_AND_EARN,
                title = "Refer & Earn",
                subtitle = if (referralCode.isNotBlank()) "Code: $referralCode • Invite friends" else "Invite friends & earn rewards",
                icon = Icons.Outlined.CardGiftcard
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.COUPONS_AND_OFFERS,
                title = "Coupons & Offers",
                subtitle = when (activeCoupons.size) {
                    0 -> "View active coupons"
                    1 -> "1 active offer available"
                    else -> "${activeCoupons.size} active offers available"
                },
                icon = Icons.Outlined.LocalOffer
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.HELP_AND_SUPPORT,
                title = "Help & Support",
                subtitle = "Order queries, quality assurance & FAQs",
                icon = Icons.AutoMirrored.Outlined.HelpOutline
            ),
            ProfileMenuRowItem(
                subpage = ProfileSubpage.ABOUT_FRESCUT,
                title = "About FRESCUT",
                subtitle = "150+ quality checks • Farm to Home cold chain",
                icon = Icons.Outlined.Info
            ),
            ProfileMenuRowItem(
                subpage = null,
                title = "Logout",
                subtitle = "Sign out of your account on this device",
                icon = Icons.AutoMirrored.Outlined.Logout,
                isLogout = true
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("profile_screen")
    ) {
        // Customer Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Account",
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp
                )
            )
        }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Information Card
            item(key = "profile_info_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                            .padding(16.dp),
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
                                    .background(FrescutRedLightBg)
                                    .border(1.dp, FrescutRedBright.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profile Avatar",
                                    tint = FrescutRedBright,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userName,
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.5.sp
                                        )
                                    )
                                    if (isProMember) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(FrescutRedBright)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PRO",
                                                color = FrescutWhite,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                if (userPhone.isNotBlank()) {
                                    Text(
                                        text = userPhone,
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                if (userEmail.isNotBlank()) {
                                    Text(
                                        text = userEmail,
                                        color = FrescutTextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Profile",
                                tint = FrescutTextPrimary
                            )
                        }
                    }
                }
            }

            // Clean Outline Icon Menu Card
            item(key = "profile_menu_list_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FrescutOutlineShape,
                    colors = CardDefaults.cardColors(containerColor = FrescutWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                    ) {
                        menuItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.isLogout) {
                                            showLogoutDialog = true
                                        } else {
                                            onOpenSubpage(item.subpage)
                                        }
                                    }
                                    .testTag("profile_menu_${item.title.lowercase().replace(" ", "_")}")
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(FrescutSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (item.isLogout) FrescutRedBright else FrescutTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            color = if (item.isLogout) FrescutRedBright else FrescutTextPrimary,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.subtitle,
                                            color = FrescutTextSecondary,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = FrescutTextSecondary
                                )
                            }

                            if (index < menuItems.lastIndex) {
                                FrescutThinDivider(
                                    startIndent = 68.dp,
                                    endIndent = 16.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(userName) }
        var phoneInput by remember { mutableStateOf(userPhone) }
        var emailInput by remember { mutableStateOf(userEmail) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = FrescutWhite,
            title = {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(nameInput, phoneInput, emailInput)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright)
                ) {
                    Text("Save Changes", color = FrescutWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = FrescutTextSecondary)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = FrescutWhite,
            title = {
                Text(
                    text = "Logout from FRESCUT?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Your saved addresses and previous order history will remain safely stored on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FrescutTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright)
                ) {
                    Text("Logout", color = FrescutWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Stay Logged In", color = FrescutTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileSubpageView(
    subpage: ProfileSubpage,
    isProMember: Boolean,
    selectedPaymentMethod: String,
    addresses: List<AddressEntity>,
    activeCoupons: List<CouponOfferModel>,
    referralCode: String,
    onBack: () -> Unit,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: (String, String, String) -> Unit,
    onTogglePro: () -> Unit,
    onSelectPaymentMethod: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAddressForm by remember(addresses.isEmpty()) { mutableStateOf(addresses.isEmpty()) }
    var newAddrLabel by remember { mutableStateOf("Home") }
    var newAddrArea by remember { mutableStateOf("") }
    var newAddrFull by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("profile_subpage_${subpage.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FrescutTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = subpage.title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 148.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (subpage) {
                ProfileSubpage.MY_ADDRESSES -> {
                    if (addresses.isEmpty()) {
                        item {
                            Text(
                                text = "No saved delivery addresses yet. Add your delivery address below.",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    items(addresses, key = { it.id }) { addr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FrescutOutlineShape)
                                .background(if (addr.isSelected) FrescutSelectedPinkBg else FrescutWhite)
                                .border(
                                    width = 1.dp,
                                    color = if (addr.isSelected) FrescutFocusRed else FrescutBorderLight,
                                    shape = FrescutOutlineShape
                                )
                                .clickable { onSelectAddress(addr.id) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${addr.label} • ${addr.areaName}",
                                        color = FrescutTextPrimary,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (addr.isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Default",
                                            color = FrescutFocusRed,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = addr.fullAddress,
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (addr.isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = FrescutFocusRed
                                )
                            }
                        }
                    }

                    item {
                        if (!showAddAddressForm) {
                            OutlinedButton(
                                onClick = { showAddAddressForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = FrescutOutlineShape
                            ) {
                                Text("+ Add New Address", color = FrescutRedBright, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(FrescutOutlineShape)
                                    .background(FrescutWhite)
                                    .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "New Delivery Address",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
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
                                            isSelected = newAddrLabel.equals(tag, ignoreCase = true),
                                            onClick = { newAddrLabel = tag }
                                        )
                                    }
                                }
                                FrescutLabeledInputField(
                                    label = "Locality / Area *",
                                    value = newAddrArea,
                                    onValueChange = { newAddrArea = it },
                                    placeholder = "e.g., Indiranagar, Koramangala"
                                )
                                FrescutLabeledInputField(
                                    label = "Complete Address & Landmark *",
                                    value = newAddrFull,
                                    onValueChange = { newAddrFull = it },
                                    placeholder = "House no., building name, street",
                                    singleLine = false
                                )
                                FrescutPrimaryButton(
                                    text = "Save Address",
                                    onClick = {
                                        if (newAddrArea.isNotBlank() && newAddrFull.isNotBlank()) {
                                            onAddAddress(newAddrLabel, newAddrArea, newAddrFull)
                                            showAddAddressForm = false
                                            newAddrArea = ""
                                            newAddrFull = ""
                                        }
                                    },
                                    enabled = newAddrArea.isNotBlank() && newAddrFull.isNotBlank()
                                )
                            }
                        }
                    }
                }

                ProfileSubpage.PAYMENT_METHODS -> {
                    val methods = listOf(
                        "Cash on Delivery"
                    )
                    items(methods) { method ->
                        val isChosen = selectedPaymentMethod == method
                        FrescutPaymentOptionCard(
                            title = method,
                            isSelected = isChosen,
                            onClick = { onSelectPaymentMethod(method) }
                        )
                    }
                }

                ProfileSubpage.FRESCUT_PRO -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FrescutRedLightBg)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, FrescutRedBright.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "FRESCUT Pro Membership",
                                    color = FrescutRedBright,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "• Zero Delivery Fee on all orders\n• Priority 30-Minute Flash Dispatch\n• Exclusive Member-Only Cuts & Combos\n• Zero Surge or Packaging Charges",
                                    color = FrescutTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = onTogglePro,
                                    colors = ButtonDefaults.buttonColors(containerColor = FrescutRedBright),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isProMember) "Pro Active (Tap to Pause)" else "Activate FRESCUT Pro",
                                        color = FrescutWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                ProfileSubpage.REFER_AND_EARN -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Refer & Earn",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            if (referralCode.isNotBlank()) {
                                Text(
                                    text = "Share your referral code with friends to unlock referral rewards on delivered orders.",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FrescutRedLightBg)
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Referral Code: $referralCode",
                                        color = FrescutRedBright,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }
                            } else {
                                Text(
                                    text = "No referral code is currently configured in Firestore (collection: config).",
                                    color = FrescutTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                ProfileSubpage.COUPONS_AND_OFFERS -> {
                    if (activeCoupons.isEmpty()) {
                        item {
                            Text(
                                text = "No active coupons available right now.",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(activeCoupons, key = { it.code }) { coupon ->
                            CouponCardRow(
                                coupon = coupon,
                                onApply = { onApplyCoupon(coupon.code) }
                            )
                        }
                    }
                }

                ProfileSubpage.HELP_AND_SUPPORT -> {
                    val faqs = listOf(
                        "How does FRESCUT maintain meat freshness?" to "Every cut is chilled between 0–4°C right from farm harvest through our temperature-controlled processing center to your doorstep within 30 minutes.",
                        "What if I'm not satisfied with a cut?" to "We offer an instant 100% no-questions-asked replacement or refund on any item that does not meet our 150+ quality checks.",
                        "Are FRESCUT chickens antibiotic-residue free?" to "Yes! All our poultry, mutton, and fish batches undergo strict lab screening and are 100% free from added hormones, preservatives, or ammonia."
                    )
                    items(faqs) { (q, a) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FrescutBorderLight, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = q,
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = a,
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                ProfileSubpage.ABOUT_FRESCUT -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FrescutBorderLight, RoundedCornerShape(16.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "FRESCUT – Farm to Home Fresh Meat & Seafood",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "FRESCUT brings hygienically processed, vacuum-sealed, 0–4°C cold-chain meat, poultry, fish, and farm eggs directly from trusted local farms to your kitchen in 30 minutes.",
                                color = FrescutTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "• 150+ Quality & Hygiene Checks\n• Custom Butcher Cuts by Master Cleavers\n• Pay Only for Net Cleaned Weight",
                                color = FrescutTextPrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponCardRow(
    coupon: CouponOfferModel,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FrescutOutlineShape)
            .background(FrescutWhite)
            .border(1.dp, FrescutBorderLight, FrescutOutlineShape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FrescutRedLightBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = coupon.code,
                    color = FrescutRedBright,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = coupon.title,
                color = FrescutTextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = coupon.subtitle,
                color = FrescutTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedButton(
            onClick = onApply,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Apply", color = FrescutRedBright, fontWeight = FontWeight.ExtraBold)
        }
    }
}
