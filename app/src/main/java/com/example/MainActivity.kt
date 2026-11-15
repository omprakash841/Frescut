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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
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
import com.example.ui.screens.AddressFormScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AuthSessionInfo
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveOrderTrackingBottomSheet
import com.example.ui.screens.LocationPickerBottomSheet
import com.example.ui.screens.LocalAuthSessionManager
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailBottomSheet
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecipeDetailBottomSheet
import com.example.ui.screens.SearchOverlayScreen
import com.example.ui.screens.signOutFromFirebase
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutWhite
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(this)
                ?: com.google.firebase.FirebaseApp.initializeApp(
                    this,
                    com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("com.aistudio.frescut.mkxzpq")
                        .setProjectId("demo-no-project")
                        .setApiKey("fake-api-key-for-emulator")
                        .build()
                )
        }
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
    var currentUser by remember {
        mutableStateOf(runCatching { Firebase.auth.currentUser }.getOrNull())
    }
    var localSession by remember { mutableStateOf(LocalAuthSessionManager.getSavedSession(context)) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        runCatching { Firebase.auth.addAuthStateListener(listener) }
        onDispose {
            runCatching { Firebase.auth.removeAuthStateListener(listener) }
        }
    }

    val activeUid = currentUser?.uid ?: localSession?.uid
    if (activeUid == null) {
        AuthScreen(
            onAuthSuccess = { sessionInfo ->
                currentUser = Firebase.auth.currentUser
                localSession = sessionInfo ?: LocalAuthSessionManager.getSavedSession(context)
            }
        )
    } else {
        key(activeUid) {
            AuthenticatedFrescutApp(
                currentUserId = activeUid,
                sessionInfo = localSession,
                onSignedOut = {
                    currentUser = null
                    localSession = null
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedFrescutApp(
    currentUserId: String,
    sessionInfo: AuthSessionInfo?,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val repository = remember(context, currentUserId) {
        val appContext = context.applicationContext
        val roomDb = FrescutDatabase.getDatabase(appContext)
        val firestoreDb = FirebaseFirestore.getInstance(
            appContext.getString(R.string.firestore_database_id)
        )
        FrescutRepository(
            db = firestoreDb,
            dao = roomDb.frescutDao(),
            sessionUserId = currentUserId
        )
    }
    val viewModel: FrescutViewModel = viewModel(
        key = currentUserId,
        factory = FrescutViewModel.Factory(repository)
    )

    LaunchedEffect(sessionInfo) {
        if (sessionInfo != null) {
            viewModel.updateUserProfile(
                name = sessionInfo.displayName,
                phone = "",
                email = sessionInfo.email
            )
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isBottomNavVisible by viewModel.isBottomNavVisible.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartProducts by viewModel.cartProducts.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val activeCoupons by viewModel.activeCoupons.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val quickCategories by viewModel.quickCategories.collectAsStateWithLifecycle()
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
    val deliveryInstructions by viewModel.deliveryInstructions.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    val isSearchOverlayOpen by viewModel.isSearchOverlayOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val selectedProductForDetail by viewModel.selectedProductForDetail.collectAsStateWithLifecycle()
    val selectedRecipeForDetail by viewModel.selectedRecipeForDetail.collectAsStateWithLifecycle()
    val isCartOpen by viewModel.isCartOpen.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val isLocationSheetOpen by viewModel.isLocationSheetOpen.collectAsStateWithLifecycle()
    val isAddressScreenOpen by viewModel.isAddressScreenOpen.collectAsStateWithLifecycle()
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
    val areaName = selectedAddress?.areaName?.takeIf { it.isNotBlank() } ?: "Add Delivery Address"
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
    if (!isCartOpen && !isSearchOverlayOpen && selectedProductForDetail == null && currentTab != MainNavTab.HOME) {
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
        // Dedicated Full-Screen Product Detail Page, Cart & Checkout, or Search Screen if active
        when {
            selectedProductForDetail != null -> {
                val product = selectedProductForDetail!!
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
                    onViewCartClick = { viewModel.openCart() }
                )
            }

            isAddressScreenOpen -> {
                AddressFormScreen(
                    addresses = addresses,
                    initialPhone = userPhone,
                    onSelectAddress = { viewModel.selectAddress(it) },
                    onSaveAddressDetails = { label, houseAndArea, floor, landmark, phone, lat, lng ->
                        viewModel.saveAddressDetails(
                            label = label,
                            houseAndArea = houseAndArea,
                            floor = floor,
                            landmark = landmark,
                            receiverPhone = phone,
                            latitude = lat,
                            longitude = lng
                        )
                    },
                    onClose = { viewModel.closeAddressScreen() }
                )
            }

            isCartOpen -> {
                CartAndCheckoutScreen(
                    cartItems = cartProducts,
                    selectedAddress = selectedAddress,
                    activeCoupons = activeCoupons,
                    appliedCoupon = appliedCoupon,
                    isProMember = isProMember,
                    appConfig = appConfig,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                    deliveryInstructions = deliveryInstructions,
                    onDeliveryInstructionsChange = { viewModel.updateDeliveryInstructions(it) },
                    onClose = { viewModel.closeCart() },
                    onAddProduct = { viewModel.addOrIncrementProduct(it) },
                    onDecrementProduct = { viewModel.decrementProduct(it) },
                    onAdjustBudget = { item, delta -> viewModel.adjustCartItemBudget(item, delta) },
                    onRemoveProduct = { prodId -> viewModel.removeProductFromCart(prodId) },
                    onApplyCouponCode = { viewModel.applyCouponCode(it) },
                    onRemoveCoupon = { viewModel.removeCoupon() },
                    onChangeAddressClick = { viewModel.openAddressScreen() },
                    onOpenAddressScreenClick = { viewModel.openAddressScreen() },
                    onAddAddress = { l, a, f -> viewModel.addAddress(l, a, f) },
                    onPlaceOrderClick = { viewModel.checkoutAndPlaceOrder() }
                )
            }

            isSearchOverlayOpen -> {
                SearchOverlayScreen(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    cartItems = cartProducts,
                    searchChips = appConfig.searchChips,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClose = { viewModel.closeSearchOverlay() },
                    onProductClick = { viewModel.openProductDetail(it) },
                    onAddProduct = { viewModel.addOrIncrementProduct(it) },
                    onDecrementProduct = { viewModel.decrementProduct(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }

            isNotificationSheetOpen -> {
                NotificationsScreen(
                    onClose = { viewModel.closeNotificationSheet() }
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
                            banners = banners,
                            recipes = recipes,
                            reviews = reviews,
                            activeCoupons = activeCoupons,
                            searchPlaceholders = appConfig.searchChips,
                            quickCategories = quickCategories,
                            onLocationClick = { viewModel.openLocationSheet() },
                            onProClick = {
                                viewModel.selectTab(MainNavTab.PROFILE)
                                viewModel.openProfileSubpage(ProfileSubpage.FRESCUT_PRO)
                            },
                            onNotificationClick = { viewModel.openNotificationSheet() },
                            onCartClick = { viewModel.openCart() },
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
                            searchPlaceholders = appConfig.searchChips,
                            onLocationClick = { viewModel.openLocationSheet() },
                            onProClick = {
                                viewModel.selectTab(MainNavTab.PROFILE)
                                viewModel.openProfileSubpage(ProfileSubpage.FRESCUT_PRO)
                            },
                            onNotificationClick = { viewModel.openNotificationSheet() },
                            onCartClick = { viewModel.openCart() },
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
                            onBrowseProductsClick = { viewModel.selectTab(MainNavTab.HOME) },
                            onRateOrder = { orderId, rating -> viewModel.rateOrder(orderId, rating) },
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
                            activeCoupons = activeCoupons,
                            referralCode = appConfig.referralCode,
                            activeSubpage = activeProfileSubpage,
                            onOpenSubpage = { viewModel.openProfileSubpage(it) },
                            onUpdateProfile = { n, p, e -> viewModel.updateUserProfile(n, p, e) },
                            onSelectAddress = { viewModel.selectAddress(it) },
                            onAddAddress = { l, a, f -> viewModel.addAddress(l, a, f) },
                            onTogglePro = { viewModel.toggleProMembership() },
                            onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                            onApplyCoupon = { viewModel.applyCouponCode(it) },
                            onLogoutConfirmed = {
                                signOutFromFirebase(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onSignOutComplete = {
                                        viewModel.selectTab(MainNavTab.HOME)
                                        onSignedOut()
                                    }
                                )
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
                onAddAddress = { l, a, f -> viewModel.addAddress(l, a, f) },
                onDismiss = { viewModel.closeLocationSheet() }
            )
        }


        // Crisp Toast Feedback Banner positioned above the bottom navigation bar
        val isMainTabScreen = selectedProductForDetail == null &&
            !isAddressScreenOpen &&
            !isCartOpen &&
            !isSearchOverlayOpen &&
            !isNotificationSheetOpen
        val toastBottomPadding = when {
            isMainTabScreen && totalCartItemsCount > 0 -> 140.dp
            else -> 84.dp
        }
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = toastBottomPadding, start = 20.dp, end = 20.dp)
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
