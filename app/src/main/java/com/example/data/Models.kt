package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val subcategory: String,
    val weightInfo: String,
    val piecesInfo: String,
    val servingsInfo: String,
    val price: Int,
    val originalPrice: Int,
    val discountPercent: Int,
    val deliveryTimeMins: Int,
    val imageRes: Int,
    val description: String,
    val isCurrentHit: Boolean = false,
    val isBudgetPick: Boolean = false,
    val isFlashDelivery: Boolean = true,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val proteinGrams: Int = 24,
    val isFavorite: Boolean = false,
    val pricePerKg: Int = 0,
    val budgetSelectorEnabled: Boolean = true
) {
    val weightAndServings: String
        get() = "$weightInfo | $servingsInfo"

    val fullWeightPiecesServings: String
        get() = "$weightInfo | $piecesInfo | $servingsInfo"

    val effectivePricePerKg: Int
        get() = if (pricePerKg > 0) pricePerKg else (price * 2).coerceAtLeast(100)
}

@Entity(tableName = "category_budget_configs")
data class CategoryBudgetConfigEntity(
    @PrimaryKey val category: String,
    val minBudget: Int,
    val incrementAmount: Int
)

@Entity(tableName = "recommendation_configs")
data class RecommendationConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = true,
    val triggerBudgetAmount: Int = 100,
    val usePastOrderHistory: Boolean = true,
    val popularRangeMultiplier: Float = 1.0f
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val selectedBudget: Int? = null,
    val estimatedGrams: Int? = null
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val status: String, // "Out for Delivery", "Processing", "Delivered"
    val placedAtText: String,
    val etaText: String,
    val itemTotal: Int,
    val deliveryFee: Int,
    val packagingCharge: Int,
    val totalAmount: Int,
    val rating: Float = 5.0f,
    val itemsEncoded: String, // pipe-delimited items: id::name::weight::price::qty::imageRes
    val timelineEncoded: String, // 5 comma-separated timestamps: placed,processing,packed,out,delivered
    val createdAtEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val areaName: String,
    val fullAddress: String,
    val houseNumber: String = "",
    val floor: String = "",
    val landmark: String = "",
    val receiverPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryMins: Int = 30,
    val isSelected: Boolean = false
)

data class CartProductItem(
    val product: ProductEntity,
    val quantity: Int,
    val selectedBudget: Int? = null,
    val estimatedGrams: Int? = null
) {
    val isCustomBudget: Boolean
        get() = selectedBudget != null && selectedBudget > 0

    val unitEffectivePrice: Int
        get() = if (isCustomBudget) selectedBudget!! else product.price

    val effectiveWeightLabel: String
        get() = if (isCustomBudget && estimatedGrams != null) {
            "~${estimatedGrams} g (Est.) • ₹${product.effectivePricePerKg}/kg"
        } else {
            product.weightInfo
        }

    val lineTotal: Int
        get() = unitEffectivePrice * quantity

    val lineOriginalTotal: Int
        get() = if (isCustomBudget) {
            val factor = if (product.price > 0) {
                product.originalPrice.toDouble() / product.price.toDouble()
            } else {
                1.1
            }
            ((selectedBudget!! * factor).toInt()) * quantity
        } else {
            product.originalPrice * quantity
        }
}

data class OrderLineItem(
    val productId: String,
    val name: String,
    val weight: String,
    val price: Int,
    val quantity: Int,
    val imageRes: Int
)

data class OrderDomainModel(
    val orderId: String,
    val status: String,
    val placedAtText: String,
    val etaText: String? = null,
    val itemTotal: Int,
    val deliveryFee: Int,
    val packagingCharge: Int,
    val totalAmount: Int,
    val rating: Float? = null,
    val items: List<OrderLineItem>,
    val timelineTimes: List<String>,
    val address: String = "",
    val houseNumber: String = "",
    val floor: String = "",
    val landmark: String = "",
    val receiverPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryInstructions: String = "",
    val paymentMethod: String = "",
    val deliveryPartner: String? = null,
    val chillerTemp: String? = null,
    val route: String? = null,
    val createdAtMillis: Long = 0L
) {
    val totalItemsCount: Int get() = items.sumOf { it.quantity }
    val hasLiveTrackingData: Boolean
        get() = !etaText.isNullOrBlank() ||
            !deliveryPartner.isNullOrBlank() ||
            !chillerTemp.isNullOrBlank() ||
            !route.isNullOrBlank()
}

data class CategoryCardModel(
    val id: String,
    val name: String,
    val itemCountText: String,
    val imageRes: Int,
    val backgroundColor: Color,
    val badgeText: String? = null,
    val subcategories: List<String> = listOf("All")
)

data class PromotionalBannerModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val highlights: List<String> = emptyList(),
    val ctaText: String = "Order Now",
    val targetCategory: String = "Chicken",
    val imageRes: Int,
    val tagText: String? = null,
    val active: Boolean = true,
    val order: Int = 0
)

data class RecipeCardModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val prepTime: String = "",
    val difficulty: String = "",
    val calories: String = "",
    val imageRes: Int,
    val primaryProductId: String = "",
    val cutName: String = "",
    val cutPrice: Int = 0,
    val steps: List<String> = emptyList(),
    val active: Boolean = true,
    val order: Int = 0
)

data class CustomerReviewModel(
    val id: String,
    val customerName: String,
    val location: String = "",
    val rating: Float = 5.0f,
    val orderedCut: String = "",
    val reviewText: String,
    val verifiedDate: String = "",
    val active: Boolean = true,
    val order: Int = 0
)

data class CouponOfferModel(
    val code: String,
    val title: String,
    val subtitle: String,
    val discountPercent: Int,
    val maxDiscount: Int,
    val minOrderValue: Int,
    val active: Boolean = true
)

data class AppConfigModel(
    val referralCode: String = "",
    val searchChips: List<String> = emptyList(),
    val storeName: String = "FRESCUT",
    val supportPhone: String = "",
    val minOrderAmount: Int = 0,
    val deliveryTimeText: String = "",
    val deliveryFee: Int? = null,
    val handlingFee: Int? = null,
    val storeLatitude: Double = 0.0,
    val storeLongitude: Double = 0.0,
    val deliveryRadiusKm: Double = 0.0
)

data class QuickNavCategoryItem(
    val id: String,
    val label: String,
    val imageRes: Int? = null,
    val isAllTab: Boolean = false,
    val isMoreTab: Boolean = false,
    val order: Int = 0
)


