package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FrescutDatabase
import com.example.data.FrescutRepository
import com.example.ui.FrescutViewModel
import com.example.ui.MainNavTab
import com.example.ui.ProfileSubpage
import com.example.ui.components.FrescutBottomNavigationBar
import com.example.ui.components.StickyCartSummaryBar
import com.example.ui.components.rememberSmartNavNestedScrollConnection
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveOrderTrackingBottomSheet
import com.example.ui.screens.LocationPickerBottomSheet
import com.example.ui.screens.NotificationsBottomSheet
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailBottomSheet
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecipeDetailBottomSheet
import com.example.ui.screens.SearchOverlayScreen
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                FrescutAppRoot()
            }
        }
    }
}

@Composable
fun FrescutAppRoot() {
    val context = LocalContext.current
    val repository = remember(context) {
        val db = FrescutDatabase.getDatabase(context)
        FrescutRepository(db.frescutDao())
    }
    val viewModel: FrescutViewModel = viewModel(
        factory = FrescutViewModel.Factory(repository)
    )

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isBottomNavVisible by viewModel.isBottomNavVisible.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartProducts by viewModel.cartProducts.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val categoryBudgetConfigs by viewModel.categoryBudgetConfigs.collectAsStateWithLifecycle()
    val recommendationConfig by viewModel.recommendationConfig.collectAsStateWithLifecycle()

    val selectedHomeCategory by viewModel.selectedHomeCategory.collectAsStateWithLifecycle()
    val showAllCurrentHitsGrid by viewModel.showAllCurrentHitsGrid.collectAsStateWithLifecycle()

    val selectedCategoryDetail by viewModel.selectedCategoryDetail.collectAsStateWithLifecycle()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()

    val selectedOrderStatusFilter by viewModel.selectedOrderStatusFilter.collectAsStateWithLifecycle()
    val selectedOrderIdForDetail by viewModel.selectedOrderIdForDetail.collectAsStateWithLifecycle()
    val liveTrackingOrder by viewModel.liveTrackingOrder.collectAsStateWithLifecycle()

    val activeProfileSubpage by viewModel.activeProfileSubpage.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isProMember by viewModel.isProMember.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    val isSearchOverlayOpen by viewModel.isSearchOverlayOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val selectedProductForDetail by viewModel.selectedProductForDetail.collectAsStateWithLifecycle()
    val selectedRecipeForDetail by viewModel.selectedRecipeForDetail.collectAsStateWithLifecycle()
    val isCartOpen by viewModel.isCartOpen.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val isLocationSheetOpen by viewModel.isLocationSheetOpen.collectAsStateWithLifecycle()
    val isNotificationSheetOpen by viewModel.isNotificationSheetOpen.collectAsStateWithLifecycle()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2300)
            viewModel.clearToast()
        }
    }

    val selectedAddress = remember(addresses) {
        addresses.find { it.isSelected } ?: addresses.firstOrNull()
    }
    val areaName = selectedAddress?.areaName ?: "Udhepali, Bargarh"
    val deliveryMins = selectedAddress?.deliveryMins ?: 30

    val totalCartItemsCount = remember(cartProducts) {
        cartProducts.sumOf { it.quantity }
    }
    val totalCartAmount = remember(cartProducts) {
        cartProducts.sumOf { it.lineTotal }
    }
    val totalCartSavings = remember(cartProducts) {
        cartProducts.sumOf { (it.lineOriginalTotal - it.lineTotal).coerceAtLeast(0) }
    }

    val smartNavNestedScroll = rememberSmartNavNestedScrollConnection { visible ->
        viewModel.setBottomNavVisible(visible)
    }

    // Back handler for secondary tabs returning to Home
    if (!isCartOpen && !isSearchOverlayOpen && currentTab != MainNavTab.HOME) {
        val hasNestedScreen = (currentTab == MainNavTab.CATEGORIES && selectedCategoryDetail != null) ||
            (currentTab == MainNavTab.ORDERS && selectedOrderIdForDetail != null) ||
            (currentTab == MainNavTab.PROFILE && activeProfileSubpage != null)
        if (!hasNestedScreen) {
            BackHandler {
                viewModel.selectTab(MainNavTab.HOME)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .nestedScroll(smartNavNestedScroll)
    ) {
        // Full-Screen Cart & Checkout or Search Overlay if open, otherwise Main Tab Screen
        when {
            isCartOpen -> {
                CartAndCheckoutScreen(
                    cartItems = cartProducts,
                    selectedAddress = selectedAddress ?: com.example.data.SeedData.initialAddresses.first(),
                    appliedCoupon = appliedCoupon,
                    isProMember = isProMember,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onClose = { viewModel.closeCart() },
                    onAddProduct = { viewModel.addOrIncrementProduct(it) },
                    onDecrementProduct = { viewModel.decrementProduct(it) },
                    onApplyCouponCode = { viewModel.applyCouponCode(it) },
                    onRemoveCoupon = { viewModel.removeCoupon() },
                    onChangeAddressClick = { viewModel.openLocationSheet() },
                    onPlaceOrderClick = { viewModel.checkoutAndPlaceOrder() }
                )
            }

            isSearchOverlayOpen -> {
                SearchOverlayScreen(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    cartItems = cartProducts,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClose = { viewModel.closeSearchOverlay() },
                    onProductClick = { viewModel.openProductDetail(it) },
                    onAddProduct = { viewModel.addOrIncrementProduct(it) },
                    onDecrementProduct = { viewModel.decrementProduct(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }

            else -> {
                when (currentTab) {
                    MainNavTab.HOME -> {
                        HomeScreen(
                            areaName = areaName,
                            deliveryMins = deliveryMins,
                            searchQuery = searchQuery,
                            selectedHomeCategory = selectedHomeCategory,
                            showAllCurrentHitsGrid = showAllCurrentHitsGrid,
                            hasUnreadNotifications = hasUnreadNotifications,
                            products = allProducts,
                            cartItems = cartProducts,
                            onLocationClick = { viewModel.openLocationSheet() },
                            onProClick = {
                                viewModel.selectTab(MainNavTab.PROFILE)
                                viewModel.openProfileSubpage(ProfileSubpage.FRESCUT_PRO)
                            },
                            onNotificationClick = { viewModel.openNotificationSheet() },
                            onSearchBarClick = { viewModel.openSearchOverlay(searchQuery) },
                            onHomeCategorySelect = { viewModel.selectHomeCategory(it) },
                            onToggleViewAllCurrentHits = { viewModel.setShowAllCurrentHitsGrid(it) },
                            onOpenCategoryDetail = { viewModel.openCategoryDetail(it) },
                            onProductClick = { viewModel.openProductDetail(it) },
                            onAddProduct = { viewModel.addOrIncrementProduct(it) },
                            onDecrementProduct = { viewModel.decrementProduct(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onApplyWelcomeCoupon = { viewModel.applyCouponCode(it) },
                            onRecipeClick = { viewModel.openRecipeDetail(it) },
                            onBottomNavVisibilityChanged = { viewModel.setBottomNavVisible(it) }
                        )
                    }

                    MainNavTab.CATEGORIES -> {
                        CategoriesScreen(
                            areaName = areaName,
                            deliveryMins = deliveryMins,
                            searchQuery = searchQuery,
                            hasUnreadNotifications = hasUnreadNotifications,
                            selectedCategoryDetail = selectedCategoryDetail,
                            selectedSubcategory = selectedSubcategory,
                            selectedSortOption = selectedSortOption,
                            products = allProducts,
                            cartItems = cartProducts,
                            onLocationClick = { viewModel.openLocationSheet() },
                            onProClick = {
                                viewModel.selectTab(MainNavTab.PROFILE)
                                viewModel.openProfileSubpage(ProfileSubpage.FRESCUT_PRO)
                            },
                            onNotificationClick = { viewModel.openNotificationSheet() },
                            onSearchBarClick = { viewModel.openSearchOverlay(searchQuery) },
                            onSelectCategory = { viewModel.openCategoryDetail(it) },
                            onBackFromCategoryDetail = { viewModel.closeCategoryDetail() },
                            onSelectSubcategory = { viewModel.selectSubcategory(it) },
                            onSelectSortOption = { viewModel.selectSortOption(it) },
                            onProductClick = { viewModel.openProductDetail(it) },
                            onAddProduct = { viewModel.addOrIncrementProduct(it) },
                            onDecrementProduct = { viewModel.decrementProduct(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onBottomNavVisibilityChanged = { viewModel.setBottomNavVisible(it) }
                        )
                    }

                    MainNavTab.ORDERS -> {
                        OrdersScreen(
                            orders = orders,
                            selectedStatusFilter = selectedOrderStatusFilter,
                            selectedOrderIdForDetail = selectedOrderIdForDetail,
                            cartCount = totalCartItemsCount,
                            onSelectStatusFilter = { viewModel.selectOrderStatusFilter(it) },
                            onOpenOrderDetail = { viewModel.openOrderDetail(it) },
                            onBackFromOrderDetail = { viewModel.closeOrderDetail() },
                            onSearchClick = { viewModel.openSearchOverlay("") },
                            onCartClick = { viewModel.openCart() },
                            onReorderClick = { viewModel.reorder(it) },
                            onTrackLiveClick = { viewModel.openLiveTracking(it) },
                            onBottomNavVisibilityChanged = { viewModel.setBottomNavVisible(it) }
                        )
                    }

                    MainNavTab.PROFILE -> {
                        ProfileScreen(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            isProMember = isProMember,
                            selectedPaymentMethod = selectedPaymentMethod,
                            addresses = addresses,
                            categoryBudgetConfigs = categoryBudgetConfigs,
                            recommendationConfig = recommendationConfig,
                            products = allProducts,
                            activeSubpage = activeProfileSubpage,
                            onOpenSubpage = { viewModel.openProfileSubpage(it) },
                            onUpdateProfile = { n, p, e -> viewModel.updateUserProfile(n, p, e) },
                            onSelectAddress = { viewModel.selectAddress(it) },
                            onAddAddress = { l, a, f -> viewModel.addAddress(l, a, f) },
                            onTogglePro = { viewModel.toggleProMembership() },
                            onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                            onApplyCoupon = { viewModel.applyCouponCode(it) },
                            onSaveCategoryBudgetConfig = { cat, minB, inc ->
                                viewModel.updateCategoryBudgetConfig(cat, minB, inc)
                            },
                            onSaveRecommendationConfig = { cfg ->
                                viewModel.updateRecommendationConfig(cfg)
                            },
                            onSaveProductBudgetSettings = { prodId, ppk, enabled ->
                                viewModel.updateProductBudgetSettings(prodId, ppk, enabled)
                            },
                            onLogoutConfirmed = {
                                viewModel.showToast("Signed out of FRESCUT")
                                viewModel.selectTab(MainNavTab.HOME)
                            },
                            onBottomNavVisibilityChanged = { viewModel.setBottomNavVisible(it) }
                        )
                    }
                }

                // Bottom Overlay: Sticky Cart Bar (when cart > 0) + Smart Floating Bottom Navigation Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    StickyCartSummaryBar(
                        itemCount = totalCartItemsCount,
                        totalAmount = totalCartAmount,
                        totalSavings = totalCartSavings,
                        onViewCartClick = { viewModel.openCart() }
                    )

                    FrescutBottomNavigationBar(
                        currentTab = currentTab,
                        isVisible = isBottomNavVisible,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        }

        // Interactive Bottom Sheets
        selectedProductForDetail?.let { product ->
            val currentProduct = allProducts.find { it.id == product.id } ?: product
            val qty = viewModel.getQuantityInCart(currentProduct.id, cartProducts)
            val categoryConfig = viewModel.getCategoryBudgetConfig(
                categoryName = currentProduct.category,
                configs = categoryBudgetConfigs
            )
            ProductDetailBottomSheet(
                product = currentProduct,
                categoryConfig = categoryConfig,
                recommendationConfig = recommendationConfig,
                pastOrders = orders,
                quantityInCart = qty,
                onDismiss = { viewModel.closeProductDetail() },
                onAddBudgetToCart = { budgetAmt ->
                    viewModel.addBudgetProductToCart(currentProduct, budgetAmt)
                },
                onAddStandardPackClick = { viewModel.addOrIncrementProduct(currentProduct) },
                onDecrementClick = { viewModel.decrementProduct(currentProduct) },
                onFavoriteToggle = { viewModel.toggleFavorite(currentProduct) },
                onViewCartClick = { viewModel.openCart() },
                onOpenAdminDashboard = { viewModel.openAdminDashboardFromAnywhere() }
            )
        }

        selectedRecipeForDetail?.let { recipe ->
            RecipeDetailBottomSheet(
                recipe = recipe,
                onDismiss = { viewModel.closeRecipeDetail() },
                onAddPrimaryCut = {
                    val target = allProducts.find { it.id == recipe.primaryProductId }
                    if (target != null) {
                        viewModel.addOrIncrementProduct(target)
                    }
                }
            )
        }

        liveTrackingOrder?.let { order ->
            LiveOrderTrackingBottomSheet(
                order = order,
                onDismiss = { viewModel.closeLiveTracking() }
            )
        }

        if (isLocationSheetOpen) {
            LocationPickerBottomSheet(
                addresses = addresses,
                onSelectAddress = { viewModel.selectAddress(it) },
                onDismiss = { viewModel.closeLocationSheet() }
            )
        }

        if (isNotificationSheetOpen) {
            NotificationsBottomSheet(
                onDismiss = { viewModel.closeNotificationSheet() },
                onApplyWelcomeCode = { viewModel.applyCouponCode("FRESH20") }
            )
        }

        // Crisp Toast Feedback Banner
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = FrescutTextPrimary.copy(alpha = 0.94f),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}
