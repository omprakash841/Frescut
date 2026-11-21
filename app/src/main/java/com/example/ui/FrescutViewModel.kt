package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AddressEntity
import com.example.data.AppConfigModel
import com.example.data.BudgetCalculator
import com.example.data.CartProductItem
import com.example.data.CategoryBudgetConfigEntity
import com.example.data.CategoryCardModel
import com.example.data.CouponOfferModel
import com.example.data.CustomerReviewModel
import com.example.data.FrescutRepository
import com.example.data.OrderDomainModel
import com.example.data.ProductEntity
import com.example.data.PromotionalBannerModel
import com.example.data.QuickNavCategoryItem
import com.example.data.RecipeCardModel
import com.example.data.RecommendationConfigEntity
import com.example.data.SeedData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab(val label: String) {
    HOME("Home"),
    CATEGORIES("Categories"),
    ORDERS("Orders"),
    PROFILE("Profile")
}

enum class SortOption(val label: String) {
    POPULAR("Popular"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    DISCOUNT("Discount %")
}

enum class ProfileSubpage(val title: String) {
    MY_ADDRESSES("My Addresses"),
    PAYMENT_METHODS("Payment Methods"),
    FRESCUT_PRO("FRESCUT Pro"),
    REFER_AND_EARN("Refer & Earn"),
    COUPONS_AND_OFFERS("Coupons & Offers"),
    HELP_AND_SUPPORT("Help & Support"),
    ABOUT_FRESCUT("About FRESCUT")
}

class FrescutViewModel(
    private val repository: FrescutRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        viewModelScope.launch {
            repository.observeUserProfile()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w("FrescutVM", "Error observing user profile", firestoreError ?: error)
                }
                .collect { profile ->
                    if (profile != null) {
                        if (profile.name.isNotBlank()) _userName.value = profile.name
                        _userPhone.value = profile.phone
                        if (profile.email.isNotBlank()) _userEmail.value = profile.email
                        _isProMember.value = profile.isProMember
                        _selectedPaymentMethod.value = "Cash on Delivery"
                        _deliveryInstructions.value = profile.deliveryInstructions
                    }
                }
        }
    }

    val allProducts: StateFlow<List<ProductEntity>> = repository.productsFlow
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w("FrescutVM", "Error observing products", firestoreError ?: error)
            emit(SeedData.initialProducts)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeedData.initialProducts
        )

    val cartProducts: StateFlow<List<CartProductItem>> = repository.cartProductsFlow
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w("FrescutVM", "Error observing cart products", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val orders: StateFlow<List<OrderDomainModel>> = repository.ordersFlow
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w("FrescutVM", "Error observing user orders", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val addresses: StateFlow<List<AddressEntity>> = repository.addressesFlow
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w("FrescutVM", "Error observing user addresses", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeCoupons: StateFlow<List<CouponOfferModel>> = repository.observeActiveCoupons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val banners: StateFlow<List<PromotionalBannerModel>> = repository.observeBanners()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recipes: StateFlow<List<RecipeCardModel>> = repository.observeRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reviews: StateFlow<List<CustomerReviewModel>> = repository.observeReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appConfig: StateFlow<AppConfigModel> = repository.observeAppConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppConfigModel()
        )

    val quickCategories: StateFlow<List<QuickNavCategoryItem>> = repository.observeQuickCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeedData.defaultQuickCategories
        )

    val categoryBudgetConfigs: StateFlow<List<CategoryBudgetConfigEntity>> =
        repository.categoryBudgetConfigsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeedData.initialCategoryBudgetConfigs
        )

    val recommendationConfig: StateFlow<RecommendationConfigEntity> =
        repository.recommendationConfigFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeedData.initialRecommendationConfig
        )

    private val _currentTab = MutableStateFlow(MainNavTab.HOME)
    val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

    // Smart Bottom Navigation visibility state
    private val _isBottomNavVisible = MutableStateFlow(true)
    val isBottomNavVisible: StateFlow<Boolean> = _isBottomNavVisible.asStateFlow()

    // Home screen state
    private val _selectedHomeCategory = MutableStateFlow("All")
    val selectedHomeCategory: StateFlow<String> = _selectedHomeCategory.asStateFlow()

    private val _showAllCurrentHitsGrid = MutableStateFlow(false)
    val showAllCurrentHitsGrid: StateFlow<Boolean> = _showAllCurrentHitsGrid.asStateFlow()

    // Categories screen state
    private val _selectedCategoryDetail = MutableStateFlow<String?>(null)
    val selectedCategoryDetail: StateFlow<String?> = _selectedCategoryDetail.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow("All")
    val selectedSubcategory: StateFlow<String> = _selectedSubcategory.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.POPULAR)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    // Orders screen state
    private val _selectedOrderStatusFilter = MutableStateFlow("All")
    val selectedOrderStatusFilter: StateFlow<String> = _selectedOrderStatusFilter.asStateFlow()

    private val _selectedOrderIdForDetail = MutableStateFlow<String?>(null)
    val selectedOrderIdForDetail: StateFlow<String?> = _selectedOrderIdForDetail.asStateFlow()

    private val _liveTrackingOrder = MutableStateFlow<OrderDomainModel?>(null)
    val liveTrackingOrder: StateFlow<OrderDomainModel?> = _liveTrackingOrder.asStateFlow()

    // Profile state
    private val _activeProfileSubpage = MutableStateFlow<ProfileSubpage?>(null)
    val activeProfileSubpage: StateFlow<ProfileSubpage?> = _activeProfileSubpage.asStateFlow()

    private val _userName = MutableStateFlow("FRESCUT Customer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isProMember = MutableStateFlow(false)
    val isProMember: StateFlow<Boolean> = _isProMember.asStateFlow()

    private val _deliveryInstructions = MutableStateFlow("")
    val deliveryInstructions: StateFlow<String> = _deliveryInstructions.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("Cash on Delivery")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()


    // Search & Modals state
    private val _isSearchOverlayOpen = MutableStateFlow(false)
    val isSearchOverlayOpen: StateFlow<Boolean> = _isSearchOverlayOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProductForDetail = MutableStateFlow<ProductEntity?>(null)
    val selectedProductForDetail: StateFlow<ProductEntity?> = _selectedProductForDetail.asStateFlow()

    private val _selectedRecipeForDetail = MutableStateFlow<RecipeCardModel?>(null)
    val selectedRecipeForDetail: StateFlow<RecipeCardModel?> = _selectedRecipeForDetail.asStateFlow()

    private val _isCartOpen = MutableStateFlow(false)
    val isCartOpen: StateFlow<Boolean> = _isCartOpen.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<CouponOfferModel?>(null)
    val appliedCoupon: StateFlow<CouponOfferModel?> = _appliedCoupon.asStateFlow()

    private val _isLocationSheetOpen = MutableStateFlow(false)
    val isLocationSheetOpen: StateFlow<Boolean> = _isLocationSheetOpen.asStateFlow()

    private val _isAddressScreenOpen = MutableStateFlow(false)
    val isAddressScreenOpen: StateFlow<Boolean> = _isAddressScreenOpen.asStateFlow()

    private val _isNotificationSheetOpen = MutableStateFlow(false)
    val isNotificationSheetOpen: StateFlow<Boolean> = _isNotificationSheetOpen.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val searchResults: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _searchQuery
    ) { products, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            products
        } else {
            products.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                    it.category.contains(trimmed, ignoreCase = true) ||
                    it.subcategory.contains(trimmed, ignoreCase = true) ||
                    it.description.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SeedData.initialProducts
    )

    fun setBottomNavVisible(visible: Boolean) {
        if (_isBottomNavVisible.value != visible) {
            _isBottomNavVisible.value = visible
        }
    }

    fun selectTab(tab: MainNavTab) {
        _currentTab.value = tab
        _isBottomNavVisible.value = true
        if (tab != MainNavTab.CATEGORIES) {
            _selectedCategoryDetail.value = null
        }
        if (tab != MainNavTab.ORDERS) {
            _selectedOrderIdForDetail.value = null
        }
        if (tab != MainNavTab.PROFILE) {
            _activeProfileSubpage.value = null
        }
    }

    fun selectHomeCategory(category: String) {
        if (category == "More") {
            _currentTab.value = MainNavTab.CATEGORIES
            _selectedCategoryDetail.value = null
            _isBottomNavVisible.value = true
        } else {
            _selectedHomeCategory.value = category
        }
    }

    fun setShowAllCurrentHitsGrid(show: Boolean) {
        _showAllCurrentHitsGrid.value = show
    }

    fun openCategoryDetail(categoryName: String) {
        _selectedCategoryDetail.value = categoryName
        _selectedSubcategory.value = "All"
        _selectedSortOption.value = SortOption.POPULAR
        _currentTab.value = MainNavTab.CATEGORIES
        _isBottomNavVisible.value = true
    }

    fun closeCategoryDetail() {
        _selectedCategoryDetail.value = null
        _isBottomNavVisible.value = true
    }

    fun selectSubcategory(subcategory: String) {
        _selectedSubcategory.value = subcategory
    }

    fun selectSortOption(option: SortOption) {
        _selectedSortOption.value = option
    }

    fun getCategoryCardModel(categoryName: String): CategoryCardModel {
        return SeedData.categories.find { it.name.equals(categoryName, ignoreCase = true) }
            ?: SeedData.categories.first()
    }

    fun getCategoryBudgetConfig(
        categoryName: String,
        configs: List<CategoryBudgetConfigEntity> = categoryBudgetConfigs.value
    ): CategoryBudgetConfigEntity {
        return configs.find { it.category.equals(categoryName, ignoreCase = true) }
            ?: SeedData.initialCategoryBudgetConfigs.find {
                it.category.equals(categoryName, ignoreCase = true)
            }
            ?: CategoryBudgetConfigEntity(
                category = categoryName,
                minBudget = if (categoryName.equals("Mutton", ignoreCase = true)) 100 else 50,
                incrementAmount = 10
            )
    }

    fun selectOrderStatusFilter(filter: String) {
        _selectedOrderStatusFilter.value = filter
    }

    fun openOrderDetail(orderId: String) {
        _selectedOrderIdForDetail.value = orderId
        _currentTab.value = MainNavTab.ORDERS
        _isBottomNavVisible.value = true
    }

    fun closeOrderDetail() {
        _selectedOrderIdForDetail.value = null
        _isBottomNavVisible.value = true
    }

    fun openLiveTracking(order: OrderDomainModel) {
        _liveTrackingOrder.value = order
    }

    fun closeLiveTracking() {
        _liveTrackingOrder.value = null
    }

    fun openProfileSubpage(subpage: ProfileSubpage?) {
        _activeProfileSubpage.value = subpage
        _isBottomNavVisible.value = true
    }

    fun openSearchOverlay(initialQuery: String = "") {
        _searchQuery.value = initialQuery
        _isSearchOverlayOpen.value = true
    }

    fun closeSearchOverlay() {
        _isSearchOverlayOpen.value = false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openProductDetail(product: ProductEntity) {
        _selectedProductForDetail.value = product
    }

    fun closeProductDetail() {
        _selectedProductForDetail.value = null
    }

    fun openRecipeDetail(recipe: RecipeCardModel) {
        _selectedRecipeForDetail.value = recipe
    }

    fun closeRecipeDetail() {
        _selectedRecipeForDetail.value = null
    }

    fun openCart() {
        _isCartOpen.value = true
    }

    fun closeCart() {
        _isCartOpen.value = false
    }

    fun openLocationSheet() {
        _isLocationSheetOpen.value = true
    }

    fun closeLocationSheet() {
        _isLocationSheetOpen.value = false
    }

    fun openAddressScreen() {
        _isLocationSheetOpen.value = false
        _isAddressScreenOpen.value = true
    }

    fun closeAddressScreen() {
        _isAddressScreenOpen.value = false
    }

    fun openNotificationSheet() {
        _hasUnreadNotifications.value = false
        _isNotificationSheetOpen.value = true
    }

    fun closeNotificationSheet() {
        _isNotificationSheetOpen.value = false
    }

    fun getQuantityInCart(productId: String, items: List<CartProductItem>): Int {
        return items.find { it.product.id == productId }?.quantity ?: 0
    }

    fun addOrIncrementProduct(product: ProductEntity) {
        val existing = cartProducts.value.find { it.product.id == product.id }
        val currentQty = existing?.quantity ?: 0
        viewModelScope.launch {
            repository.setCartQuantity(
                productId = product.id,
                newQuantity = currentQty + 1,
                selectedBudget = existing?.selectedBudget,
                estimatedGrams = existing?.estimatedGrams
            )
            if (currentQty == 0) {
                showToast("${product.name} added to cart")
            }
        }
    }

    fun addBudgetProductToCart(product: ProductEntity, selectedBudget: Int) {
        val estimatedGrams = BudgetCalculator.calculateEstimatedGrams(
            selectedBudget = selectedBudget,
            pricePerKg = product.effectivePricePerKg
        )
        viewModelScope.launch {
            repository.setCartQuantity(
                productId = product.id,
                newQuantity = 1,
                selectedBudget = selectedBudget,
                estimatedGrams = estimatedGrams
            )
            showToast("${product.name} (₹$selectedBudget • ~${estimatedGrams}g Est.) added to cart")
        }
    }

    fun decrementProduct(product: ProductEntity) {
        val existing = cartProducts.value.find { it.product.id == product.id }
        val currentQty = existing?.quantity ?: 0
        viewModelScope.launch {
            repository.setCartQuantity(
                productId = product.id,
                newQuantity = (currentQty - 1).coerceAtLeast(0),
                selectedBudget = existing?.selectedBudget,
                estimatedGrams = existing?.estimatedGrams
            )
        }
    }

    fun removeProductFromCart(productId: String) {
        viewModelScope.launch {
            repository.setCartQuantity(productId = productId, newQuantity = 0)
            showToast("Item removed from cart")
        }
    }

    fun adjustCartItemBudget(item: CartProductItem, deltaInr: Int) {
        val catConfig = getCategoryBudgetConfig(item.product.category)
        val currentBudget = item.selectedBudget ?: item.product.price
        val nextBudget = (currentBudget + deltaInr).coerceAtLeast(catConfig.minBudget)
        val nextGrams = BudgetCalculator.calculateEstimatedGrams(
            selectedBudget = nextBudget,
            pricePerKg = item.product.effectivePricePerKg
        )
        viewModelScope.launch {
            repository.setCartQuantity(
                productId = item.product.id,
                newQuantity = item.quantity.coerceAtLeast(1),
                selectedBudget = nextBudget,
                estimatedGrams = nextGrams
            )
        }
    }

    fun updateDeliveryInstructions(instructions: String) {
        _deliveryInstructions.value = instructions
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(product)
            if (_selectedProductForDetail.value?.id == product.id) {
                _selectedProductForDetail.value = product.copy(isFavorite = !product.isFavorite)
            }
        }
    }

    fun applyCouponCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            showToast("Please enter a coupon code")
            return
        }
        val found = activeCoupons.value.find {
            it.active && it.code.equals(trimmed, ignoreCase = true)
        }
        if (found != null) {
            _appliedCoupon.value = found
            showToast("Coupon ${found.code} (${found.discountPercent}% OFF) applied!")
        } else {
            showToast("Invalid or inactive coupon code")
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun selectAddress(addressId: Int) {
        viewModelScope.launch {
            repository.selectAddress(addressId)
            _isLocationSheetOpen.value = false
            showToast("Delivery location updated")
        }
    }

    fun addAddress(label: String, areaName: String, fullAddress: String) {
        val cleanArea = areaName.trim()
        val cleanFull = fullAddress.trim()
        if (cleanArea.isEmpty() || cleanFull.isEmpty()) {
            showToast("Please enter both area name and full address")
            return
        }
        viewModelScope.launch {
            try {
                repository.addNewAddress(label, cleanArea, cleanFull)
                _isLocationSheetOpen.value = false
                _isAddressScreenOpen.value = false
                showToast("Address details saved")
            } catch (e: Exception) {
                showToast("Could not save address: ${e.localizedMessage ?: "Error"}")
            }
        }
    }

    fun saveAddressDetails(
        label: String,
        houseAndArea: String,
        floor: String,
        landmark: String,
        receiverPhone: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val cleanHouseArea = houseAndArea.trim()
        val cleanFloor = floor.trim()
        val cleanLandmark = landmark.trim()
        val cleanPhone = receiverPhone.trim()
        if (cleanHouseArea.isEmpty() || cleanPhone.isEmpty()) {
            showToast("House number / Area and Receiver's phone number are required")
            return
        }
        val shortArea = cleanHouseArea.split(",")
            .map { it.trim() }
            .lastOrNull { it.isNotEmpty() }
            ?.take(60)
            ?: cleanHouseArea.take(60)
        val parts = buildList {
            add(cleanHouseArea)
            if (cleanFloor.isNotEmpty()) add(cleanFloor)
            if (cleanLandmark.isNotEmpty()) add(cleanLandmark)
        }
        val formattedFull = parts.joinToString(", ")
        viewModelScope.launch {
            try {
                repository.addNewAddress(
                    label = label,
                    areaName = shortArea,
                    fullAddress = formattedFull,
                    houseNumber = cleanHouseArea,
                    floor = cleanFloor,
                    landmark = cleanLandmark,
                    receiverPhone = cleanPhone,
                    latitude = latitude,
                    longitude = longitude
                )
                if (_userPhone.value.isBlank()) {
                    _userPhone.value = cleanPhone
                }
                _isAddressScreenOpen.value = false
                _isLocationSheetOpen.value = false
                showToast("Address details saved")
            } catch (e: Exception) {
                showToast("Could not save address: ${e.localizedMessage ?: "Error"}")
            }
        }
    }

    fun updateUserProfile(name: String, phone: String, email: String) {
        _userName.value = name
        _userPhone.value = phone
        _userEmail.value = email
        viewModelScope.launch {
            try {
                repository.updateUserProfileInCloud(
                    name = name,
                    phone = phone,
                    email = email,
                    isProMember = _isProMember.value,
                    selectedPaymentMethod = _selectedPaymentMethod.value,
                    deliveryInstructions = _deliveryInstructions.value
                )
                showToast("Profile synced to cloud")
            } catch (e: Exception) {
                showToast("Profile updated locally")
            }
        }
    }

    fun toggleProMembership() {
        _isProMember.value = !_isProMember.value
        val msg = if (_isProMember.value) "FRESCUT Pro activated!" else "FRESCUT Pro paused"
        viewModelScope.launch {
            try {
                repository.updateUserProfileInCloud(
                    name = _userName.value,
                    phone = _userPhone.value,
                    email = _userEmail.value,
                    isProMember = _isProMember.value,
                    selectedPaymentMethod = _selectedPaymentMethod.value,
                    deliveryInstructions = _deliveryInstructions.value
                )
            } catch (unused: Exception) {
            }
        }
        showToast(msg)
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
        viewModelScope.launch {
            try {
                repository.updateUserProfileInCloud(
                    name = _userName.value,
                    phone = _userPhone.value,
                    email = _userEmail.value,
                    isProMember = _isProMember.value,
                    selectedPaymentMethod = method,
                    deliveryInstructions = _deliveryInstructions.value
                )
            } catch (unused: Exception) {
            }
        }
        showToast("Payment method set to $method")
    }

    fun reorder(order: OrderDomainModel) {
        viewModelScope.launch {
            repository.reorderItems(order)
            val countLabel = if (order.items.size == 1) "1 item" else "${order.items.size} items"
            showToast("$countLabel from ${order.orderId} added to cart")
            _isCartOpen.value = true
        }
    }

    fun checkoutAndPlaceOrder() {
        val currentCart = cartProducts.value
        if (currentCart.isEmpty()) return
        val selectedAddr = addresses.value.find { it.isSelected } ?: addresses.value.firstOrNull()
        val addressText = selectedAddr?.fullAddress?.trim().orEmpty()
        if (addressText.isEmpty()) {
            _isAddressScreenOpen.value = true
            showToast("Please add delivery address details")
            return
        }

        val subtotal = currentCart.sumOf { it.lineTotal }
        val coupon = _appliedCoupon.value?.takeIf { applied ->
            activeCoupons.value.any { it.active && it.code.equals(applied.code, ignoreCase = true) }
        }
        val couponDisc = if (coupon != null && subtotal >= coupon.minOrderValue) {
            ((subtotal * coupon.discountPercent) / 100).coerceAtMost(coupon.maxDiscount)
        } else {
            0
        }
        val config = appConfig.value
        if (config.deliveryRadiusKm > 0.0) {
            val custLat = selectedAddr?.latitude
            val custLng = selectedAddr?.longitude
            if (custLat == null || custLng == null) {
                showToast("Abhi hum is area me delivery nahi karte")
                return
            }
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                config.storeLatitude,
                config.storeLongitude,
                custLat,
                custLng,
                results
            )
            val distanceKm = results[0].toDouble() / 1000.0
            if (distanceKm > config.deliveryRadiusKm) {
                showToast("Abhi hum is area me delivery nahi karte")
                return
            }
        }
        val deliveryFee = (config.deliveryFee ?: 0).coerceAtLeast(0)
        val handlingFee = (config.handlingFee ?: 0).coerceAtLeast(0)
        val houseNumberVal = selectedAddr?.houseNumber?.takeIf { it.isNotBlank() } ?: addressText
        val floorVal = selectedAddr?.floor.orEmpty()
        val landmarkVal = selectedAddr?.landmark.orEmpty()
        val receiverPhoneVal = selectedAddr?.receiverPhone?.takeIf { it.isNotBlank() } ?: _userPhone.value

        viewModelScope.launch {
            try {
                val newOrderId = repository.placeOrder(
                    cartItems = currentCart,
                    discountAmount = couponDisc,
                    deliveryFee = deliveryFee,
                    handlingFee = handlingFee,
                    addressText = addressText,
                    houseNumber = houseNumberVal,
                    floor = floorVal,
                    landmark = landmarkVal,
                    receiverPhone = receiverPhoneVal,
                    latitude = selectedAddr?.latitude,
                    longitude = selectedAddr?.longitude,
                    deliveryInstructions = _deliveryInstructions.value,
                    paymentMethod = _selectedPaymentMethod.value
                )
                _appliedCoupon.value = null
                _isCartOpen.value = false
                if (newOrderId.isNotEmpty()) {
                    _selectedOrderStatusFilter.value = "All"
                    _selectedOrderIdForDetail.value = null
                    _currentTab.value = MainNavTab.ORDERS
                    _isBottomNavVisible.value = true
                    showToast("Order $newOrderId placed!")
                }
            } catch (e: Exception) {
                showToast("Order failed: ${e.localizedMessage ?: "Please check connection"}")
            }
        }
    }


    fun rateOrder(orderId: String, ratingValue: Int) {
        viewModelScope.launch {
            try {
                repository.rateOrder(orderId, ratingValue)
                showToast("Thank you! Rated $ratingValue ★ for $orderId")
            } catch (e: Exception) {
                showToast("Could not save rating: ${e.localizedMessage ?: "Error"}")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showToast(message: String) {
        _toastMessage.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    class Factory(private val repository: FrescutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FrescutViewModel(repository) as T
        }
    }
}
