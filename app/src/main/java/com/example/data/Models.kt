package com.example.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlin.math.roundToInt

data class BudgetRecommendationChip(
    val budgetAmount: Int,
    val estimatedGrams: Int,
    val tagLabel: String
)

/**
 * Core budget-to-weight calculator.
 * Formula: EstimatedGrams = (SelectedBudget / PricePerKg) * 1000
 * Example: Price ₹260/kg, Budget ₹50 -> (50 / 260.0) * 1000 = 192.3g -> ~192g
 */
object BudgetCalculator {
    const val STEP_RUPEES: Int = 10
    const val RECOMMENDATION_TRIGGER_BUDGET: Int = 100

    fun calculateEstimatedGrams(selectedBudget: Int, pricePerKg: Int): Int {
        if (pricePerKg <= 0 || selectedBudget <= 0) return 0
        return ((selectedBudget.toDouble() / pricePerKg.toDouble()) * 1000.0)
            .roundToInt()
            .coerceAtLeast(1)
    }

    fun formatWeightDisplay(grams: Int): String {
        return if (grams >= 1000) {
            val kg = grams / 1000.0
            val formatted = if (grams % 1000 == 0) {
                "${grams / 1000} kg"
            } else {
                String.format(java.util.Locale.US, "%.2f kg", kg)
            }
            "Approx. $formatted (Estimated)"
        } else {
            "Approx. $grams g (Estimated)"
        }
    }

    fun defaultMinimumBudgetForCategory(categoryName: String): Int {
        return when (categoryName.trim().lowercase()) {
            "mutton", "value packs", "value_packs" -> 100
            else -> 50
        }
    }

    fun generateRecommendations(
        product: ProductEntity,
        categoryConfig: CategoryBudgetConfigEntity,
        recommendationConfig: RecommendationConfigEntity,
        pastOrders: List<OrderDomainModel> = emptyList()
    ): List<BudgetRecommendationChip> {
        val pricePerKg = product.effectivePricePerKg
        val minBudget = categoryConfig.minBudget.coerceAtLeast(50)
        val step = categoryConfig.incrementAmount.coerceAtLeast(10)
        val base1 = maxOf(120, minBudget + step * 2)
        val base2 = maxOf(150, base1 + 30)
        val base3 = maxOf(200, base2 + 50)

        val pastBudgets = if (recommendationConfig.usePastOrderHistory && pastOrders.isNotEmpty()) {
            pastOrders.flatMap { it.items }
                .filter { it.productId == product.id && it.price >= minBudget }
                .map { it.price }
        } else {
            emptyList()
        }

        val rec1 = pastBudgets.firstOrNull() ?: base1
        val rec2 = if (rec1 == base2) base2 + 20 else base2
        val rec3 = maxOf(base3, rec2 + 40)

        return listOf(
            BudgetRecommendationChip(
                budgetAmount = rec1,
                estimatedGrams = calculateEstimatedGrams(rec1, pricePerKg),
                tagLabel = "Popular Choice"
            ),
            BudgetRecommendationChip(
                budgetAmount = rec2,
                estimatedGrams = calculateEstimatedGrams(rec2, pricePerKg),
                tagLabel = "Best Value"
            ),
            BudgetRecommendationChip(
                budgetAmount = rec3,
                estimatedGrams = calculateEstimatedGrams(rec3, pricePerKg),
                tagLabel = "Family Pack"
            )
        )
    }
}

@Entity(tableName = "category_budget_configs")
data class CategoryBudgetConfigEntity(
    @PrimaryKey val category: String,
    val minBudget: Int = 50,
    val incrementAmount: Int = 10
) {
    val categoryId: String get() = category
    val categoryName: String get() = category
    val minimumStartPrice: Int get() = minBudget
}

@Entity(tableName = "recommendation_configs")
data class RecommendationConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = true,
    val triggerBudgetAmount: Int = 100,
    val usePastOrderHistory: Boolean = true,
    val popularRangeMultiplier: Float = 1.0f,
    val recommendationOption1: Int = 100,
    val recommendationOption2: Int = 150,
    val recommendationOption3: Int = 200
) {
    fun optionsList(): List<Int> = listOf(
        recommendationOption1,
        recommendationOption2,
        recommendationOption3
    )
}

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
    val deliveryTimeMins: Int = 30,
    @DrawableRes val imageRes: Int,
    val description: String = "",
    val isCurrentHit: Boolean = false,
    val isBudgetPick: Boolean = false,
    val proteinGrams: Int = 24,
    val pricePerKg: Int = 0,
    val budgetSelectorEnabled: Boolean = true,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val isFavorite: Boolean = false,
    val available: Boolean = true,
    val hindiSubtitle: String = "",
    val storageInstructions: String = "Keep chilled between 0°C and 4°C. Consume within 24 hours of opening."
) {
    val categoryId: String get() = category
    val weight: String get() = weightInfo
    val piecesOrServes: String get() = "$piecesInfo • $servingsInfo"
    val deliveryMinutes: Int get() = deliveryTimeMins
    val isBestseller: Boolean get() = isCurrentHit
    val isExpressDelivery: Boolean get() = deliveryTimeMins <= 35
    val isHighProtein: Boolean get() = proteinGrams >= 20
    val isBoneless: Boolean
        get() = name.contains("Boneless", ignoreCase = true) ||
            subcategory.contains("Boneless", ignoreCase = true) ||
            subcategory.contains("Breast", ignoreCase = true)
    val isCurryCut: Boolean
        get() = name.contains("Curry", ignoreCase = true) ||
            subcategory.contains("Curry", ignoreCase = true)

    val effectivePricePerKg: Int
        get() = if (pricePerKg > 0) pricePerKg else (price * 2).coerceAtLeast(100)
}

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val selectedBudget: Int? = null,
    val estimatedGrams: Int? = null
)

data class CartProductItem(
    val product: ProductEntity,
    val quantity: Int,
    val selectedBudget: Int? = null,
    val estimatedGrams: Int? = null,
    val priceUpdated: Boolean = false
) {
    val isCustomBudget: Boolean
        get() = selectedBudget != null && selectedBudget > 0

    val unitEffectivePrice: Int
        get() = if (selectedBudget != null && selectedBudget > 0) selectedBudget else product.price

    val lineTotal: Int
        get() = unitEffectivePrice * quantity

    val lineOriginalTotal: Int
        get() {
            if (product.originalPrice <= product.price || product.price <= 0) {
                return lineTotal
            }
            return if (isCustomBudget) {
                val ratio = product.originalPrice.toDouble() / product.price.toDouble()
                (unitEffectivePrice * ratio * quantity).roundToInt().coerceAtLeast(lineTotal)
            } else {
                product.originalPrice * quantity
            }
        }

    val effectiveWeightLabel: String
        get() {
            val grams = estimatedGrams ?: if (isCustomBudget) {
                BudgetCalculator.calculateEstimatedGrams(unitEffectivePrice, product.effectivePricePerKg)
            } else {
                null
            }
            return if (isCustomBudget && grams != null && grams > 0) {
                "~$grams g (Budget ₹$unitEffectivePrice)"
            } else {
                "${product.weightInfo} • ${product.piecesInfo}"
            }
        }
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val placedAtText: String,
    val status: String,
    val itemSummary: String,
    val totalItemsCount: Int,
    val itemTotal: Int,
    val deliveryFee: Int,
    val packagingCharge: Int,
    val totalAmount: Int,
    val paymentMethod: String,
    val address: String,
    val houseNumber: String = "",
    val floor: String = "",
    val landmark: String = "",
    val receiverPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryInstructions: String = "",
    val rating: Float? = null,
    val timelineTimesCsv: String = "",
    val itemsJson: String = ""
)

data class OrderLineItem(
    val productId: String,
    val name: String,
    val weight: String,
    val quantity: Int,
    val price: Int,
    @DrawableRes val imageRes: Int
)

data class OrderDomainModel(
    val orderId: String,
    val placedAtText: String,
    val createdAtMillis: Long? = null,
    val status: String,
    val itemSummary: String,
    val totalItemsCount: Int,
    val itemTotal: Int,
    val deliveryFee: Int,
    val packagingCharge: Int,
    val totalAmount: Int,
    val paymentMethod: String,
    val address: String,
    val houseNumber: String = "",
    val floor: String = "",
    val landmark: String = "",
    val receiverPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryInstructions: String = "",
    val rating: Float? = null,
    val timelineTimes: List<String> = listOf("", "", "", "", ""),
    val items: List<OrderLineItem> = emptyList(),
    val etaText: String? = null,
    val deliveryPartner: String? = null,
    val chillerTemp: String? = null,
    val route: String? = null
) {
    val isActiveOrder: Boolean
        get() {
            val norm = status.trim().lowercase()
            return norm == "order placed" ||
                norm == "processing" ||
                norm == "packed" ||
                norm == "out for delivery"
        }

    val hasLiveTrackingData: Boolean
        get() = !etaText.isNullOrBlank() ||
            !deliveryPartner.isNullOrBlank() ||
            !chillerTemp.isNullOrBlank() ||
            !route.isNullOrBlank()
}

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreDocId: String = "",
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

data class QuickNavCategoryItem(
    val id: String,
    val label: String,
    @DrawableRes val imageRes: Int = R.drawable.img_cat_chicken,
    val isAllTab: Boolean = false,
    val isMoreTab: Boolean = false,
    val order: Int = 0
)

data class CategoryCardModel(
    val id: String,
    val name: String,
    val itemCountText: String = "",
    @DrawableRes val imageRes: Int,
    val backgroundColor: Color,
    val badgeText: String? = null,
    val subcategories: List<String> = listOf("All")
) {
    val itemCount: String get() = itemCountText
    val pastelColor: Color get() = backgroundColor
}

data class PromotionalBannerModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val highlights: List<String> = emptyList(),
    val ctaText: String = "Order Now",
    val targetCategory: String = "Chicken",
    @DrawableRes val imageRes: Int = R.drawable.img_hero_farm_to_home_1794299674996,
    val active: Boolean = true,
    val order: Int = 1
)

data class RecipeCardModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val prepTime: String,
    val difficulty: String,
    val calories: String = "350 kcal",
    @DrawableRes val imageRes: Int,
    val primaryProductId: String = "prod_chicken_breast",
    val cutName: String = "Fresh Cut",
    val cutPrice: Int = 299,
    val steps: List<String> = emptyList(),
    val active: Boolean = true,
    val order: Int = 1
) {
    val cutProductId: String get() = primaryProductId
}

data class CustomerReviewModel(
    val id: String,
    val customerName: String,
    val location: String = "",
    val rating: Float = 5.0f,
    val reviewText: String,
    val orderedCut: String = "",
    val active: Boolean = true,
    val order: Int = 1
) {
    val comment: String get() = reviewText
    val purchasedCut: String get() = orderedCut
}

data class CouponOfferModel(
    val code: String,
    val title: String = "",
    val subtitle: String = "",
    val discountPercent: Int = 10,
    val maxDiscount: Int = 200,
    val minOrderValue: Int = 0,
    val active: Boolean = false
) {
    val description: String get() = subtitle
    val discountAmount: Int get() = maxDiscount
}

data class AppConfigModel(
    val referralCode: String = "",
    val searchChips: List<String> = listOf("Chicken", "Mutton", "Fish", "Eggs"),
    val storeName: String = "FRESCUT",
    val supportPhone: String = "",
    val minOrderAmount: Int = 0,
    val deliveryTimeText: String = "",
    val deliveryFee: Int? = null,
    val handlingFee: Int? = null,
    val aboutText: String = "",
    val fssaiNumber: String = "",
    val storeLatitude: Double = 0.0,
    val storeLongitude: Double = 0.0,
    val deliveryRadiusKm: Double = 0.0
)

enum class ProfileSubpage {
    NONE,
    ADDRESSES,
    PAYMENTS,
    COUPONS,
    REFER_EARN,
    HELP_SUPPORT,
    ABOUT_US
}

// Firestore DTOs and mapping helpers

data class FirestoreProduct(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "Chicken",
    val subcategory: String = "All",
    val weightInfo: String = "500 g",
    val piecesInfo: String = "",
    val servingsInfo: String = "",
    val price: Long = 0L,
    val originalPrice: Long = 0L,
    val discountPercent: Long = 0L,
    val deliveryTimeMins: Long = 30L,
    val imageKey: String = "",
    val description: String = "",
    val isCurrentHit: Boolean = false,
    val isBudgetPick: Boolean = false,
    val proteinGrams: Long = 24L,
    val pricePerKg: Long = 0L,
    val budgetSelectorEnabled: Boolean = true,
    val rating: Double = 0.0,
    val reviewCount: Long = 0L,
    val available: Boolean = true,
    val ownerId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toProductEntity(isFavorite: Boolean = false): ProductEntity {
        val fallbackSeed = SeedData.initialProducts.find { it.id == id }
        val resolvedPrice = price.toInt().takeIf { it > 0 } ?: fallbackSeed?.price ?: 250
        val resolvedOriginalPrice = originalPrice.toInt().takeIf { it > 0 } ?: fallbackSeed?.originalPrice ?: resolvedPrice
        val resolvedPricePerKg = pricePerKg.toInt().takeIf { it > 0 }
            ?: fallbackSeed?.pricePerKg
            ?: (resolvedPrice * 2)
        return ProductEntity(
            id = id.ifBlank { fallbackSeed?.id ?: "prod_unknown" },
            name = name.ifBlank { fallbackSeed?.name ?: "Fresh Cut" },
            category = category.ifBlank { fallbackSeed?.category ?: "Chicken" },
            subcategory = subcategory.ifBlank { fallbackSeed?.subcategory ?: "All" },
            weightInfo = weightInfo.ifBlank { fallbackSeed?.weightInfo ?: "500 g" },
            piecesInfo = piecesInfo.ifBlank { fallbackSeed?.piecesInfo ?: "Fresh Cut" },
            servingsInfo = servingsInfo.ifBlank { fallbackSeed?.servingsInfo ?: "Serves 2–3" },
            price = resolvedPrice,
            originalPrice = resolvedOriginalPrice,
            discountPercent = discountPercent.toInt(),
            deliveryTimeMins = deliveryTimeMins.toInt().takeIf { it > 0 } ?: 30,
            imageRes = DrawableKeyMapper.fromKey(imageKey, fallbackSeed?.imageRes ?: R.drawable.img_prod_chicken_breast),
            description = description.ifBlank { fallbackSeed?.description ?: "" },
            isCurrentHit = isCurrentHit,
            isBudgetPick = isBudgetPick,
            proteinGrams = proteinGrams.toInt().takeIf { it > 0 } ?: fallbackSeed?.proteinGrams ?: 24,
            pricePerKg = resolvedPricePerKg,
            budgetSelectorEnabled = budgetSelectorEnabled,
            rating = rating.toFloat(),
            reviewCount = reviewCount.toInt(),
            isFavorite = isFavorite,
            available = available
        )
    }
}

data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val isProMember: Boolean = false,
    val selectedPaymentMethod: String = "Cash on Delivery",
    val deliveryInstructions: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class FirestoreUserAddress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val label: String = "Home",
    val areaName: String = "",
    val fullAddress: String = "",
    val houseNumber: String = "",
    val floor: String = "",
    val landmark: String = "",
    val receiverPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryMins: Long = 30L,
    val isSelected: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

object DrawableKeyMapper {
    fun toKey(@DrawableRes resId: Int): String = when (resId) {
        R.drawable.img_prod_chicken_breast -> "img_prod_chicken_breast"
        R.drawable.img_prod_chicken_curry_cut -> "img_prod_chicken_curry_cut"
        R.drawable.img_prod_chicken_drumstick -> "img_prod_chicken_drumstick"
        R.drawable.img_prod_chicken_thigh -> "img_prod_chicken_thigh"
        R.drawable.img_prod_chicken_wings -> "img_prod_chicken_wings"
        R.drawable.img_prod_mutton_curry -> "img_prod_mutton_curry"
        R.drawable.img_prod_rohu_fish -> "img_prod_rohu_fish"
        R.drawable.img_prod_prawns_seafood_1794299676701 -> "img_prod_prawns_seafood"
        R.drawable.img_cat_chicken -> "img_cat_chicken"
        R.drawable.img_cat_mutton -> "img_cat_mutton"
        R.drawable.img_cat_fish -> "img_cat_fish"
        R.drawable.img_cat_eggs -> "img_cat_eggs"
        R.drawable.img_cat_organ_meat -> "img_cat_organ_meat"
        R.drawable.img_cat_ready_to_cook -> "img_cat_ready_to_cook"
        R.drawable.img_cat_sausages -> "img_cat_sausages"
        R.drawable.img_cat_marinated -> "img_cat_marinated"
        R.drawable.img_cat_value_packs -> "img_cat_value_packs"
        R.drawable.img_hero_farm_to_home_1794299674996 -> "img_hero_farm_to_home"
        R.drawable.img_combo_packs_meat -> "img_combo_packs_meat"
        R.drawable.img_recipe_butter_chicken_1794299674369 -> "img_recipe_butter_chicken"
        R.drawable.img_recipe_rogan_josh_1794299676591 -> "img_recipe_rogan_josh"
        else -> "img_prod_chicken_breast"
    }

    @DrawableRes
    fun fromKey(key: String?, @DrawableRes defaultRes: Int = R.drawable.img_prod_chicken_breast): Int {
        val clean = key?.trim()?.lowercase().orEmpty()
        return when {
            clean.isBlank() -> defaultRes
            clean.contains("mutton_curry") || clean == "mutton" -> R.drawable.img_prod_mutton_curry
            clean.contains("rogan_josh") -> R.drawable.img_recipe_rogan_josh_1794299676591
            clean.contains("butter_chicken") -> R.drawable.img_recipe_butter_chicken_1794299674369
            clean.contains("prawns") || clean.contains("seafood") -> R.drawable.img_prod_prawns_seafood_1794299676701
            clean.contains("rohu") || clean.contains("fish") && clean.contains("prod") -> R.drawable.img_prod_rohu_fish
            clean.contains("drumstick") -> R.drawable.img_prod_chicken_drumstick
            clean.contains("thigh") -> R.drawable.img_prod_chicken_thigh
            clean.contains("wings") -> R.drawable.img_prod_chicken_wings
            clean.contains("curry_cut") -> R.drawable.img_prod_chicken_curry_cut
            clean.contains("chicken_breast") -> R.drawable.img_prod_chicken_breast
            clean.contains("cat_chicken") || clean == "chicken" -> R.drawable.img_cat_chicken
            clean.contains("cat_mutton") -> R.drawable.img_cat_mutton
            clean.contains("cat_fish") || clean == "fish" -> R.drawable.img_cat_fish
            clean.contains("cat_eggs") || clean.contains("egg") -> R.drawable.img_cat_eggs
            clean.contains("organ") -> R.drawable.img_cat_organ_meat
            clean.contains("ready_to_cook") || clean.contains("snacks") -> R.drawable.img_cat_ready_to_cook
            clean.contains("sausage") -> R.drawable.img_cat_sausages
            clean.contains("marinated") -> R.drawable.img_cat_marinated
            clean.contains("value_pack") || clean.contains("combo") -> R.drawable.img_combo_packs_meat
            clean.contains("hero") || clean.contains("farm") -> R.drawable.img_hero_farm_to_home_1794299674996
            else -> defaultRes
        }
    }
}

fun ProductEntity.toFirestorePayload(ownerId: String): Map<String, Any> = mapOf(
    "name" to name,
    "category" to category,
    "subcategory" to subcategory,
    "weightInfo" to weightInfo,
    "piecesInfo" to piecesInfo,
    "servingsInfo" to servingsInfo,
    "price" to price.toLong(),
    "originalPrice" to originalPrice.toLong(),
    "discountPercent" to discountPercent.toLong(),
    "deliveryTimeMins" to deliveryTimeMins.toLong(),
    "imageKey" to DrawableKeyMapper.toKey(imageRes),
    "description" to description,
    "isCurrentHit" to isCurrentHit,
    "isBudgetPick" to isBudgetPick,
    "proteinGrams" to proteinGrams.toLong(),
    "pricePerKg" to effectivePricePerKg.toLong(),
    "budgetSelectorEnabled" to budgetSelectorEnabled,
    "rating" to rating.toDouble(),
    "reviewCount" to reviewCount.toLong(),
    "available" to available,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun QuickNavCategoryItem.toFirestoreCategoryPayload(ownerId: String): Map<String, Any> = mapOf(
    "id" to id,
    "name" to label,
    "label" to label,
    "imageUrl" to DrawableKeyMapper.toKey(imageRes),
    "isAllTab" to isAllTab,
    "isMoreTab" to isMoreTab,
    "order" to order.toLong(),
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun AppConfigModel.toFirestoreAppConfigPayload(ownerId: String): Map<String, Any> = mapOf(
    "searchChips" to searchChips,
    "referralCode" to referralCode,
    "storeName" to storeName,
    "supportPhone" to supportPhone,
    "minOrderAmount" to minOrderAmount.toLong(),
    "storeLatitude" to storeLatitude,
    "storeLongitude" to storeLongitude,
    "deliveryRadiusKm" to deliveryRadiusKm,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun PromotionalBannerModel.toFirestoreBannerPayload(ownerId: String): Map<String, Any> = mapOf(
    "title" to title,
    "subtitle" to subtitle,
    "buttonText" to ctaText,
    "ctaText" to ctaText,
    "imageUrl" to DrawableKeyMapper.toKey(imageRes),
    "targetCategory" to targetCategory,
    "highlights" to highlights,
    "active" to active,
    "order" to order.toLong(),
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun CouponOfferModel.toFirestoreCouponPayload(ownerId: String): Map<String, Any> = mapOf(
    "code" to code,
    "title" to title,
    "subtitle" to subtitle,
    "discountPercent" to discountPercent.toLong(),
    "maxDiscount" to maxDiscount.toLong(),
    "minOrder" to minOrderValue.toLong(),
    "minOrderValue" to minOrderValue.toLong(),
    "active" to active,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun RecipeCardModel.toFirestoreRecipePayload(ownerId: String): Map<String, Any> = mapOf(
    "title" to title,
    "subtitle" to subtitle,
    "time" to prepTime,
    "prepTime" to prepTime,
    "difficulty" to difficulty,
    "calories" to calories,
    "imageUrl" to DrawableKeyMapper.toKey(imageRes),
    "primaryProductId" to primaryProductId,
    "cutName" to cutName,
    "cutPrice" to cutPrice.toLong(),
    "steps" to steps,
    "active" to active,
    "order" to order.toLong(),
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun DocumentSnapshot.toCouponOfferModel(): CouponOfferModel? {
    val codeVal = (getString("code") ?: id).trim().uppercase()
    if (codeVal.isBlank()) return null
    val discPct = (getLong("discountPercent") ?: 10L).toInt()
    val minOrd = (getLong("minOrder") ?: getLong("minOrderValue") ?: 0L).toInt()
    val maxDisc = (getLong("maxDiscount") ?: 200L).toInt()
    val activeFlag = getBoolean("active") ?: false
    val titleVal = getString("title")?.takeIf { it.isNotBlank() } ?: "$discPct% OFF"
    val subtitleVal = getString("subtitle")?.takeIf { it.isNotBlank() }
        ?: "Flat $discPct% OFF${if (minOrd > 0) " on orders above ₹$minOrd" else ""}"
    return CouponOfferModel(
        code = codeVal,
        title = titleVal,
        subtitle = subtitleVal,
        discountPercent = discPct,
        maxDiscount = maxDisc,
        minOrderValue = minOrd,
        active = activeFlag
    )
}

fun DocumentSnapshot.toPromotionalBannerModel(): PromotionalBannerModel? {
    val titleVal = getString("title")?.trim().orEmpty()
    if (titleVal.isBlank()) return null
    val subtitleVal = getString("subtitle")?.trim().orEmpty()
    val ctaVal = (getString("buttonText") ?: getString("ctaText") ?: "Order Now").trim()
    val imageKeyVal = getString("imageUrl") ?: getString("imageKey") ?: ""
    val targetCat = (getString("targetCategory") ?: "Chicken").trim()
    val activeVal = getBoolean("active") ?: true
    val orderVal = (getLong("order") ?: 1L).toInt()
    val rawHighlights = (get("highlights") as? List<*>)?.mapNotNull { it?.toString()?.trim()?.takeIf { s -> s.isNotEmpty() } }
        ?: listOf("Hygienic", "Fresh", "Trusted")
    val defaultRes = if (titleVal.contains("Combo", ignoreCase = true)) {
        R.drawable.img_combo_packs_meat
    } else {
        R.drawable.img_hero_farm_to_home_1794299674996
    }
    return PromotionalBannerModel(
        id = id,
        title = titleVal,
        subtitle = subtitleVal,
        highlights = rawHighlights,
        ctaText = ctaVal,
        targetCategory = targetCat,
        imageRes = DrawableKeyMapper.fromKey(imageKeyVal, defaultRes),
        active = activeVal,
        order = orderVal
    )
}

fun DocumentSnapshot.toRecipeCardModel(): RecipeCardModel? {
    val titleVal = getString("title")?.trim().orEmpty()
    if (titleVal.isBlank()) return null
    val seedMatch = SeedData.initialRecipesSeed.find { it.title.equals(titleVal, ignoreCase = true) || it.id == id }
    val timeVal = (getString("time") ?: getString("prepTime") ?: seedMatch?.prepTime ?: "25 mins").trim()
    val diffVal = (getString("difficulty") ?: seedMatch?.difficulty ?: "Easy").trim()
    val subtitleVal = (getString("subtitle") ?: seedMatch?.subtitle ?: "Chef-crafted fresh meat recipe").trim()
    val caloriesVal = (getString("calories") ?: seedMatch?.calories ?: "380 kcal").trim()
    val imageKeyVal = getString("imageUrl") ?: getString("imageKey") ?: ""
    val activeVal = getBoolean("active") ?: true
    val orderVal = (getLong("order") ?: seedMatch?.order?.toLong() ?: 1L).toInt()
    val stepsList = (get("steps") as? List<*>)?.mapNotNull { it?.toString()?.trim()?.takeIf { s -> s.isNotEmpty() } }
        ?.takeIf { it.isNotEmpty() }
        ?: seedMatch?.steps
        ?: listOf("Prepare fresh cut with spices and cook until tender.")
    return RecipeCardModel(
        id = id,
        title = titleVal,
        subtitle = subtitleVal,
        prepTime = timeVal,
        difficulty = diffVal,
        calories = caloriesVal,
        imageRes = DrawableKeyMapper.fromKey(
            imageKeyVal,
            seedMatch?.imageRes ?: R.drawable.img_recipe_butter_chicken_1794299674369
        ),
        primaryProductId = getString("primaryProductId") ?: seedMatch?.primaryProductId ?: "prod_chicken_breast",
        cutName = getString("cutName") ?: seedMatch?.cutName ?: "Chicken Breast Boneless",
        cutPrice = (getLong("cutPrice") ?: seedMatch?.cutPrice?.toLong() ?: 412L).toInt(),
        steps = stepsList,
        active = activeVal,
        order = orderVal
    )
}

fun DocumentSnapshot.toCustomerReviewModel(): CustomerReviewModel? {
    val nameVal = (getString("name") ?: getString("customerName") ?: "").trim()
    val textVal = (getString("text") ?: getString("reviewText") ?: getString("comment") ?: "").trim()
    if (nameVal.isBlank() || textVal.isBlank()) return null
    val ratingVal = (getDouble("rating") ?: 5.0).toFloat()
    val activeVal = getBoolean("active") ?: true
    val orderVal = (getLong("order") ?: 1L).toInt()
    val locationVal = (getString("location") ?: "").trim()
    val cutVal = (getString("orderedCut") ?: getString("purchasedCut") ?: "Fresh Cut").trim()
    return CustomerReviewModel(
        id = id,
        customerName = nameVal,
        location = locationVal,
        rating = ratingVal,
        reviewText = textVal,
        orderedCut = cutVal,
        active = activeVal,
        order = orderVal
    )
}

fun DocumentSnapshot.toAppConfigModel(): AppConfigModel {
    val chips = (get("searchChips") as? List<*>)
        ?.mapNotNull { it?.toString()?.trim()?.takeIf { s -> s.isNotEmpty() } }
        ?: emptyList()
    val refCode = (getString("referralCode") ?: "").trim()
    val storeNameVal = (getString("storeName") ?: "FRESCUT").trim()
    val supportPhoneVal = (getString("supportPhone") ?: "").trim()
    val minOrderVal = (getLong("minOrderAmount") ?: 0L).toInt()
    val deliveryTimeVal = (getString("deliveryTimeText") ?: "").trim()
    val delFeeVal = getLong("deliveryFee")?.toInt()
    val handFeeVal = (getLong("handlingFee") ?: getLong("packagingCharge"))?.toInt()
    val storeLat = getDouble("storeLatitude") ?: 0.0
    val storeLng = getDouble("storeLongitude") ?: 0.0
    val radiusKm = getDouble("deliveryRadiusKm") ?: 0.0
    return AppConfigModel(
        referralCode = refCode,
        searchChips = chips,
        storeName = storeNameVal,
        supportPhone = supportPhoneVal,
        minOrderAmount = minOrderVal,
        deliveryTimeText = deliveryTimeVal,
        deliveryFee = delFeeVal,
        handlingFee = handFeeVal,
        storeLatitude = storeLat,
        storeLongitude = storeLng,
        deliveryRadiusKm = radiusKm
    )
}

fun DocumentSnapshot.toQuickNavCategoryItem(): QuickNavCategoryItem? {
    val labelVal = (getString("name") ?: getString("label") ?: "").trim()
    if (labelVal.isBlank()) return null
    val idVal = (getString("id") ?: labelVal).trim()
    val imageKeyVal = getString("imageUrl") ?: getString("image") ?: getString("imageKey") ?: idVal
    val orderVal = (getLong("order") ?: 99L).toInt()
    val isAll = getBoolean("isAllTab") ?: idVal.equals("All", ignoreCase = true)
    val isMore = getBoolean("isMoreTab") ?: idVal.equals("More", ignoreCase = true)
    return QuickNavCategoryItem(
        id = idVal,
        label = labelVal,
        imageRes = DrawableKeyMapper.fromKey(imageKeyVal, R.drawable.img_cat_chicken),
        isAllTab = isAll,
        isMoreTab = isMore,
        order = orderVal
    )
}

fun DocumentSnapshot.toOrderDomainModel(): OrderDomainModel? {
    val rawOrderNum = (getString("orderNumber") ?: id).trim()
    if (rawOrderNum.isBlank()) return null
    val normalizedOrderId = if (rawOrderNum.startsWith("#")) rawOrderNum else "#$rawOrderNum"
    val rawItems = (get("items") as? List<*>)?.mapNotNull { entry ->
        val map = entry as? Map<*, *> ?: return@mapNotNull null
        val prodId = map["productId"]?.toString() ?: "prod_item"
        val name = map["name"]?.toString() ?: "Fresh Cut"
        val weight = map["weight"]?.toString() ?: "500 g"
        val qty = (map["quantity"] as? Number)?.toInt() ?: 1
        val price = (map["price"] as? Number)?.toInt() ?: 0
        val imgKey = map["image"]?.toString()
        OrderLineItem(
            productId = prodId,
            name = name,
            weight = weight,
            quantity = qty,
            price = price,
            imageRes = DrawableKeyMapper.fromKey(imgKey, R.drawable.img_prod_chicken_breast)
        )
    } ?: emptyList()

    val createdAtTs = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    val createdAtMs = createdAtTs?.toDate()?.time
    val placedTimeText = if (createdAtTs != null) {
        java.text.SimpleDateFormat("d MMM, h:mm a", java.util.Locale.US).format(createdAtTs.toDate())
    } else {
        getString("time") ?: ""
    }

    val totalAmt = (getLong("total") ?: getLong("totalAmount") ?: 0L).toInt()
    val itemSub = (getLong("itemTotal") ?: rawItems.sumOf { it.price * it.quantity }.toLong()).toInt()
    val delFee = (getLong("deliveryFee") ?: 0L).toInt()
    val packFee = (getLong("packagingCharge") ?: getLong("handlingFee") ?: 0L).toInt()

    val timeline = (get("timelineTimes") as? List<*>)?.map { it?.toString().orEmpty() }
        ?.let { list ->
            if (list.size >= 5) list.take(5) else list + List(5 - list.size) { "" }
        }
        ?: listOf(placedTimeText, "", "", "", "")

    return OrderDomainModel(
        orderId = normalizedOrderId,
        placedAtText = placedTimeText,
        createdAtMillis = createdAtMs,
        status = (getString("status") ?: "Order Placed").trim(),
        itemSummary = rawItems.joinToString(", ") { "${it.quantity}x ${it.name}" },
        totalItemsCount = rawItems.sumOf { it.quantity }.coerceAtLeast(1),
        itemTotal = itemSub,
        deliveryFee = delFee,
        packagingCharge = packFee,
        totalAmount = totalAmt,
        paymentMethod = getString("paymentMethod") ?: "Cash on Delivery",
        address = getString("address") ?: "",
        houseNumber = getString("houseNumber") ?: "",
        floor = getString("floor") ?: "",
        landmark = getString("landmark") ?: "",
        receiverPhone = getString("receiverPhone") ?: "",
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        deliveryInstructions = getString("deliveryInstructions") ?: "",
        rating = getDouble("rating")?.toFloat(),
        timelineTimes = timeline,
        items = rawItems,
        etaText = getString("etaText")?.takeIf { it.isNotBlank() },
        deliveryPartner = getString("deliveryPartner")?.takeIf { it.isNotBlank() },
        chillerTemp = getString("chillerTemp")?.takeIf { it.isNotBlank() },
        route = getString("route")?.takeIf { it.isNotBlank() }
    )
}
