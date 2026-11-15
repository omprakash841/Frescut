package com.example.data

import android.util.Log
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = try {
        FirebaseAuth.getInstance()
    } catch (unused: Exception) {
        null
    }
    val currentUser = auth?.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

object DrawableKeyMapper {
    fun toKey(resId: Int): String = when (resId) {
        R.drawable.img_prod_chicken_breast -> "img_prod_chicken_breast"
        R.drawable.img_prod_mutton_curry -> "img_prod_mutton_curry"
        R.drawable.img_prod_chicken_drumstick -> "img_prod_chicken_drumstick"
        R.drawable.img_prod_rohu_fish -> "img_prod_rohu_fish"
        R.drawable.img_prod_chicken_thigh -> "img_prod_chicken_thigh"
        R.drawable.img_prod_chicken_wings -> "img_prod_chicken_wings"
        R.drawable.img_prod_chicken_curry_cut -> "img_prod_chicken_curry_cut"
        R.drawable.img_cat_eggs -> "img_cat_eggs"
        R.drawable.img_prod_prawns_seafood_1794299676701 -> "img_prod_prawns_seafood_1794299676701"
        R.drawable.img_cat_organ_meat -> "img_cat_organ_meat"
        R.drawable.img_cat_ready_to_cook -> "img_cat_ready_to_cook"
        R.drawable.img_cat_sausages -> "img_cat_sausages"
        R.drawable.img_cat_marinated -> "img_cat_marinated"
        R.drawable.img_combo_packs_meat -> "img_combo_packs_meat"
        R.drawable.img_cat_chicken -> "img_cat_chicken"
        R.drawable.img_cat_mutton -> "img_cat_mutton"
        R.drawable.img_cat_fish -> "img_cat_fish"
        R.drawable.img_cat_value_packs -> "img_cat_value_packs"
        R.drawable.img_hero_farm_to_home_1794299674996 -> "img_hero_farm_to_home_1794299674996"
        R.drawable.img_recipe_butter_chicken_1794299674369 -> "img_recipe_butter_chicken_1794299674369"
        R.drawable.img_recipe_rogan_josh_1794299676591 -> "img_recipe_rogan_josh_1794299676591"
        R.drawable.img_gift_box_reward -> "img_gift_box_reward"
        else -> "img_prod_chicken_breast"
    }

    fun toResId(key: String): Int = when (key) {
        "img_prod_chicken_breast" -> R.drawable.img_prod_chicken_breast
        "img_prod_mutton_curry" -> R.drawable.img_prod_mutton_curry
        "img_prod_chicken_drumstick" -> R.drawable.img_prod_chicken_drumstick
        "img_prod_rohu_fish" -> R.drawable.img_prod_rohu_fish
        "img_prod_chicken_thigh" -> R.drawable.img_prod_chicken_thigh
        "img_prod_chicken_wings" -> R.drawable.img_prod_chicken_wings
        "img_prod_chicken_curry_cut" -> R.drawable.img_prod_chicken_curry_cut
        "img_cat_eggs" -> R.drawable.img_cat_eggs
        "img_prod_prawns_seafood_1794299676701" -> R.drawable.img_prod_prawns_seafood_1794299676701
        "img_cat_organ_meat" -> R.drawable.img_cat_organ_meat
        "img_cat_ready_to_cook" -> R.drawable.img_cat_ready_to_cook
        "img_cat_sausages" -> R.drawable.img_cat_sausages
        "img_cat_marinated" -> R.drawable.img_cat_marinated
        "img_combo_packs_meat" -> R.drawable.img_combo_packs_meat
        "img_cat_chicken" -> R.drawable.img_cat_chicken
        "img_cat_mutton" -> R.drawable.img_cat_mutton
        "img_cat_fish" -> R.drawable.img_cat_fish
        "img_cat_value_packs" -> R.drawable.img_cat_value_packs
        "img_hero_farm_to_home_1794299674996" -> R.drawable.img_hero_farm_to_home_1794299674996
        "img_recipe_butter_chicken_1794299674369" -> R.drawable.img_recipe_butter_chicken_1794299674369
        "img_recipe_rogan_josh_1794299676591" -> R.drawable.img_recipe_rogan_josh_1794299676591
        "img_gift_box_reward" -> R.drawable.img_gift_box_reward
        else -> R.drawable.img_prod_chicken_breast
    }
}

@IgnoreExtraProperties
data class FirestoreProduct(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "Chicken",
    val subcategory: String = "All",
    val pricePerKg: Long = 500L,
    val price: Long = 250L,
    val originalPrice: Long = 280L,
    val discountPercent: Long = 10L,
    val deliveryTimeMins: Long = 30L,
    val image: String = "img_prod_chicken_breast",
    val available: Boolean = true,
    val weightInfo: String = "500 g",
    val piecesInfo: String = "6-8 Pieces",
    val servingsInfo: String = "Serves 3-4",
    val description: String = "Fresh hygienic cut",
    val isCurrentHit: Boolean = false,
    val isBudgetPick: Boolean = false,
    val isFlashDelivery: Boolean = true,
    val rating: Double = 0.0,
    val reviewCount: Long = 0L,
    val proteinGrams: Long = 24L,
    val budgetSelectorEnabled: Boolean = true,
    val ownerId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toProductEntity(isFavorite: Boolean = false): ProductEntity {
        val docId = id.ifBlank { "prod_${name.lowercase().replace(Regex("[^a-z0-9]+"), "_")}" }
        return ProductEntity(
            id = docId,
            name = name,
            category = category,
            subcategory = subcategory,
            weightInfo = weightInfo,
            piecesInfo = piecesInfo,
            servingsInfo = servingsInfo,
            price = price.toInt(),
            originalPrice = originalPrice.toInt(),
            discountPercent = discountPercent.toInt(),
            deliveryTimeMins = deliveryTimeMins.toInt(),
            imageRes = DrawableKeyMapper.toResId(image),
            description = description,
            isCurrentHit = isCurrentHit,
            isBudgetPick = isBudgetPick,
            isFlashDelivery = isFlashDelivery,
            rating = rating.toFloat(),
            reviewCount = reviewCount.toInt(),
            proteinGrams = proteinGrams.toInt(),
            isFavorite = isFavorite,
            pricePerKg = pricePerKg.toInt(),
            budgetSelectorEnabled = budgetSelectorEnabled
        )
    }
}

fun ProductEntity.toFirestorePayload(ownerId: String): Map<String, Any> = mapOf(
    "name" to name,
    "category" to category,
    "subcategory" to subcategory,
    "pricePerKg" to effectivePricePerKg.toLong(),
    "price" to price.toLong(),
    "originalPrice" to originalPrice.toLong(),
    "discountPercent" to discountPercent.toLong(),
    "deliveryTimeMins" to deliveryTimeMins.toLong(),
    "image" to DrawableKeyMapper.toKey(imageRes),
    "available" to true,
    "weightInfo" to weightInfo,
    "piecesInfo" to piecesInfo,
    "servingsInfo" to servingsInfo,
    "description" to description,
    "isCurrentHit" to isCurrentHit,
    "isBudgetPick" to isBudgetPick,
    "isFlashDelivery" to isFlashDelivery,
    "rating" to rating.toDouble(),
    "reviewCount" to reviewCount.toLong(),
    "proteinGrams" to proteinGrams.toLong(),
    "budgetSelectorEnabled" to budgetSelectorEnabled,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isProMember: Boolean = false,
    val selectedPaymentMethod: String = "Cash on Delivery",
    val deliveryInstructions: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
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
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

fun DocumentSnapshot.toOrderDomainModel(): OrderDomainModel? {
    val data = this.data ?: return null
    val rawOrderNum = (data["orderNumber"] as? String)?.trim().orEmpty().ifBlank { this.id }
    val orderNumber = if (rawOrderNum.startsWith("#")) rawOrderNum else "#$rawOrderNum"
    val status = (data["status"] as? String)?.takeIf { it.isNotBlank() } ?: "Order Placed"
    val etaText = (data["etaText"] as? String)?.takeIf { it.isNotBlank() }
    val itemTotal = (data["itemTotal"] as? Number)?.toInt() ?: 0
    val deliveryFee = (data["deliveryFee"] as? Number)?.toInt() ?: 0
    val packagingCharge = (data["packagingCharge"] as? Number)?.toInt() ?: 0
    val total = (data["total"] as? Number)?.toInt() ?: itemTotal
    val rating = (data["rating"] as? Number)?.toFloat()
    val address = (data["address"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    val houseNumber = (data["houseNumber"] as? String)?.trim() ?: ""
    val floor = (data["floor"] as? String)?.trim() ?: ""
    val landmark = (data["landmark"] as? String)?.trim() ?: ""
    val receiverPhone = (data["receiverPhone"] as? String)?.trim() ?: ""
    val latitude = (data["latitude"] as? Number)?.toDouble()
    val longitude = (data["longitude"] as? Number)?.toDouble()
    val deliveryInstructions = (data["deliveryInstructions"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    val paymentMethod = (data["paymentMethod"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    val deliveryPartner = (data["deliveryPartner"] as? String)?.takeIf { it.isNotBlank() }
    val chillerTemp = (data["chillerTemp"] as? String)?.takeIf { it.isNotBlank() }
    val route = (data["route"] as? String)?.takeIf { it.isNotBlank() }

    val rawItems = data["items"] as? List<*> ?: emptyList<Any>()
    val items = rawItems.mapNotNull { entry ->
        val itemMap = entry as? Map<*, *> ?: return@mapNotNull null
        val prodId = (itemMap["productId"] as? String) ?: "prod_chicken_breast"
        val name = (itemMap["name"] as? String) ?: "Fresh Cut"
        val weight = (itemMap["weight"] as? String) ?: "500 g"
        val price = (itemMap["price"] as? Number)?.toInt() ?: 0
        val qty = (itemMap["quantity"] as? Number)?.toInt() ?: 1
        val imageKey = (itemMap["image"] as? String) ?: "img_prod_chicken_breast"
        OrderLineItem(
            productId = prodId,
            name = name,
            weight = weight,
            price = price,
            quantity = qty,
            imageRes = DrawableKeyMapper.toResId(imageKey)
        )
    }

    val rawTimeline = data["timelineTimes"] as? List<*> ?: emptyList<Any>()
    val timelineTimes = List(5) { idx -> (rawTimeline.getOrNull(idx) as? String) ?: "" }

    val createdAtDate = this.getTimestamp(
        "createdAt",
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
    )?.toDate()
    val createdAtMillis = createdAtDate?.time ?: 0L
    val placedAtText = if (createdAtDate != null) {
        java.text.SimpleDateFormat("d MMM, h:mm a", java.util.Locale.US).format(createdAtDate)
    } else {
        ((data["time"] as? String)?.replace(Regex("^Today,\\s*", RegexOption.IGNORE_CASE), "")?.trim()).orEmpty()
    }

    return OrderDomainModel(
        orderId = orderNumber,
        status = status,
        placedAtText = placedAtText,
        etaText = etaText,
        itemTotal = itemTotal,
        deliveryFee = deliveryFee,
        packagingCharge = packagingCharge,
        totalAmount = total,
        rating = rating,
        items = items,
        timelineTimes = timelineTimes,
        address = address,
        houseNumber = houseNumber,
        floor = floor,
        landmark = landmark,
        receiverPhone = receiverPhone,
        latitude = latitude,
        longitude = longitude,
        deliveryInstructions = deliveryInstructions,
        paymentMethod = paymentMethod,
        deliveryPartner = deliveryPartner,
        chillerTemp = chillerTemp,
        route = route,
        createdAtMillis = createdAtMillis
    )
}

fun DocumentSnapshot.toCouponOfferModel(): CouponOfferModel? {
    val data = this.data ?: return null
    val active = (data["active"] as? Boolean) ?: false
    if (!active) return null
    val code = ((data["code"] as? String) ?: this.id).trim()
    if (code.isBlank()) return null
    val discountPercent = (data["discountPercent"] as? Number)?.toInt() ?: 0
    if (discountPercent <= 0) return null
    val minOrderValue = ((data["minOrder"] as? Number) ?: (data["minOrderValue"] as? Number))?.toInt() ?: 0
    val title = (data["title"] as? String)?.takeIf { it.isNotBlank() } ?: "Offer: $code"
    val subtitle = (data["subtitle"] as? String)?.takeIf { it.isNotBlank() }
        ?: if (minOrderValue > 0) "Valid on orders above ₹$minOrderValue" else "Flat $discountPercent% OFF on your order"
    val maxDiscount = (data["maxDiscount"] as? Number)?.toInt() ?: 500
    return CouponOfferModel(
        code = code,
        title = title,
        subtitle = subtitle,
        discountPercent = discountPercent,
        maxDiscount = maxDiscount,
        minOrderValue = minOrderValue,
        active = true
    )
}

fun DocumentSnapshot.toPromotionalBannerModel(): PromotionalBannerModel? {
    val data = this.data ?: return null
    val active = (data["active"] as? Boolean) ?: true
    if (!active) return null
    val title = (data["title"] as? String)?.takeIf { it.isNotBlank() } ?: return null
    val subtitle = (data["subtitle"] as? String)?.trim() ?: ""
    val rawHighlights = data["highlights"] as? List<*> ?: emptyList<Any>()
    val highlights = rawHighlights.mapNotNull { (it as? String)?.takeIf { s -> s.isNotBlank() } }
    val ctaText = ((data["buttonText"] as? String) ?: (data["ctaText"] as? String))
        ?.takeIf { it.isNotBlank() } ?: "Order Now"
    val targetCategory = (data["targetCategory"] as? String)?.takeIf { it.isNotBlank() }
        ?: if (title.contains("Combo", ignoreCase = true)) "Value Packs" else "Chicken"
    val imageKey = ((data["imageUrl"] as? String) ?: (data["image"] as? String))
        ?.takeIf { it.isNotBlank() }
        ?: if (title.contains("Combo", ignoreCase = true)) "img_combo_packs_meat" else "img_hero_farm_to_home_1794299674996"
    val tagText = (data["tagText"] as? String)?.takeIf { it.isNotBlank() }
    val order = (data["order"] as? Number)?.toInt() ?: 0
    return PromotionalBannerModel(
        id = this.id,
        title = title,
        subtitle = subtitle,
        highlights = highlights,
        ctaText = ctaText,
        targetCategory = targetCategory,
        imageRes = DrawableKeyMapper.toResId(imageKey),
        tagText = tagText,
        active = true,
        order = order
    )
}

fun DocumentSnapshot.toRecipeCardModel(): RecipeCardModel? {
    val data = this.data ?: return null
    val active = (data["active"] as? Boolean) ?: true
    if (!active) return null
    val title = (data["title"] as? String)?.takeIf { it.isNotBlank() } ?: return null
    val prepTime = ((data["time"] as? String) ?: (data["prepTime"] as? String))
        ?.takeIf { it.isNotBlank() } ?: "30 mins"
    val difficulty = (data["difficulty"] as? String)?.takeIf { it.isNotBlank() } ?: "Easy"
    val order = (data["order"] as? Number)?.toInt() ?: 0

    // Match default metadata from SeedData.initialRecipesSeed if optional fields are omitted in Firestore
    val defaultRecipe = SeedData.initialRecipesSeed.firstOrNull {
        it.title.equals(title, ignoreCase = true)
    }
    val subtitle = (data["subtitle"] as? String)?.takeIf { it.isNotBlank() }
        ?: defaultRecipe?.subtitle ?: "Chef-curated home recipe"
    val calories = (data["calories"] as? String)?.takeIf { it.isNotBlank() }
        ?: defaultRecipe?.calories ?: "350 kcal"
    val imageKey = ((data["imageUrl"] as? String) ?: (data["image"] as? String))
        ?.takeIf { it.isNotBlank() }
        ?: defaultRecipe?.let { DrawableKeyMapper.toKey(it.imageRes) }
        ?: "img_recipe_butter_chicken_1794299674369"
    val primaryProductId = (data["primaryProductId"] as? String)?.takeIf { it.isNotBlank() }
        ?: defaultRecipe?.primaryProductId ?: "prod_chicken_breast"
    val cutName = (data["cutName"] as? String)?.takeIf { it.isNotBlank() }
        ?: defaultRecipe?.cutName ?: "Chicken Breast Boneless"
    val cutPrice = (data["cutPrice"] as? Number)?.toInt()
        ?: defaultRecipe?.cutPrice ?: 412
    val rawSteps = data["steps"] as? List<*>
    val steps = rawSteps?.mapNotNull { (it as? String)?.takeIf { s -> s.isNotBlank() } }
        ?.takeIf { it.isNotEmpty() }
        ?: defaultRecipe?.steps
        ?: listOf(
            "Season the fresh FRESCUT cut with spices and herbs.",
            "Pan-sear or simmer on medium flame until tender and cooked through.",
            "Garnish with fresh coriander and serve hot."
        )

    return RecipeCardModel(
        id = this.id,
        title = title,
        subtitle = subtitle,
        prepTime = prepTime,
        difficulty = difficulty,
        calories = calories,
        imageRes = DrawableKeyMapper.toResId(imageKey),
        primaryProductId = primaryProductId,
        cutName = cutName,
        cutPrice = cutPrice,
        steps = steps,
        active = true,
        order = order
    )
}

fun DocumentSnapshot.toCustomerReviewModel(): CustomerReviewModel? {
    val data = this.data ?: return null
    val active = (data["active"] as? Boolean) ?: true
    if (!active) return null
    val customerName = ((data["name"] as? String) ?: (data["customerName"] as? String))
        ?.takeIf { it.isNotBlank() } ?: return null
    val reviewText = ((data["text"] as? String) ?: (data["reviewText"] as? String))
        ?.takeIf { it.isNotBlank() } ?: return null
    val rating = (data["rating"] as? Number)?.toFloat() ?: 5.0f
    val order = (data["order"] as? Number)?.toInt() ?: 0
    val location = (data["location"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    val orderedCut = (data["orderedCut"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    val verifiedDate = (data["verifiedDate"] as? String)?.takeIf { it.isNotBlank() } ?: ""
    return CustomerReviewModel(
        id = this.id,
        customerName = customerName,
        location = location,
        rating = rating,
        orderedCut = orderedCut,
        reviewText = reviewText,
        verifiedDate = verifiedDate,
        active = true,
        order = order
    )
}

fun DocumentSnapshot.toAppConfigModel(): AppConfigModel {
    val data = this.data ?: return AppConfigModel()
    val referralCode = (data["referralCode"] as? String)?.trim() ?: ""
    val rawChips = data["searchChips"] as? List<*> ?: emptyList<Any>()
    val searchChips = rawChips.mapNotNull { (it as? String)?.trim()?.takeIf { s -> s.isNotEmpty() } }
    val storeName = (data["storeName"] as? String)?.trim()?.ifBlank { "FRESCUT" } ?: "FRESCUT"
    val supportPhone = (data["supportPhone"] as? String)?.trim() ?: ""
    val minOrderAmount = (data["minOrderAmount"] as? Number)?.toInt() ?: 0
    val deliveryTimeText = (data["deliveryTimeText"] as? String)?.trim() ?: ""
    val deliveryFee = (data["deliveryFee"] as? Number)?.toInt()
    val handlingFee = (data["handlingFee"] as? Number)?.toInt()
    val storeLatitude = (data["storeLatitude"] as? Number)?.toDouble() ?: 0.0
    val storeLongitude = (data["storeLongitude"] as? Number)?.toDouble() ?: 0.0
    val deliveryRadiusKm = (data["deliveryRadiusKm"] as? Number)?.toDouble() ?: 0.0
    return AppConfigModel(
        referralCode = referralCode,
        searchChips = searchChips,
        storeName = storeName,
        supportPhone = supportPhone,
        minOrderAmount = minOrderAmount,
        deliveryTimeText = deliveryTimeText,
        deliveryFee = deliveryFee,
        handlingFee = handlingFee,
        storeLatitude = storeLatitude,
        storeLongitude = storeLongitude,
        deliveryRadiusKm = deliveryRadiusKm
    )
}

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
    "imageUrl" to DrawableKeyMapper.toKey(imageRes),
    "active" to active,
    "order" to order.toLong(),
    "targetCategory" to targetCategory,
    "highlights" to highlights,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun CouponOfferModel.toFirestoreCouponPayload(ownerId: String): Map<String, Any> = mapOf(
    "code" to code,
    "discountPercent" to discountPercent.toLong(),
    "minOrder" to minOrderValue.toLong(),
    "active" to active,
    "title" to title,
    "subtitle" to subtitle,
    "maxDiscount" to maxDiscount.toLong(),
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun RecipeCardModel.toFirestoreRecipePayload(ownerId: String): Map<String, Any> = mapOf(
    "title" to title,
    "time" to prepTime,
    "difficulty" to difficulty,
    "imageUrl" to DrawableKeyMapper.toKey(imageRes),
    "active" to active,
    "order" to order.toLong(),
    "subtitle" to subtitle,
    "calories" to calories,
    "primaryProductId" to primaryProductId,
    "cutName" to cutName,
    "cutPrice" to cutPrice.toLong(),
    "steps" to steps,
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun DocumentSnapshot.toQuickNavCategoryItem(): QuickNavCategoryItem? {
    val data = this.data ?: return null
    val rawName = ((data["name"] as? String) ?: (data["label"] as? String) ?: this.id).trim()
    if (rawName.isBlank()) return null
    val isAllTab = (data["isAllTab"] as? Boolean) ?: rawName.equals("All", ignoreCase = true)
    val isMoreTab = (data["isMoreTab"] as? Boolean) ?: rawName.equals("More", ignoreCase = true)
    val imageKey = (data["image"] as? String)?.trim().orEmpty()
    val order = (data["order"] as? Number)?.toInt() ?: when {
        isAllTab -> 1
        rawName.equals("Chicken", ignoreCase = true) -> 2
        rawName.equals("Mutton", ignoreCase = true) -> 3
        rawName.equals("Fish", ignoreCase = true) -> 4
        rawName.equals("Eggs", ignoreCase = true) -> 5
        rawName.equals("Ready to Cook", ignoreCase = true) -> 6
        isMoreTab -> 999
        else -> 50
    }
    val resolvedImageRes = when {
        isAllTab || isMoreTab -> null
        imageKey.isNotBlank() -> DrawableKeyMapper.toResId(imageKey)
        rawName.contains("Chicken", ignoreCase = true) -> R.drawable.img_cat_chicken
        rawName.contains("Mutton", ignoreCase = true) -> R.drawable.img_cat_mutton
        rawName.contains("Fish", ignoreCase = true) -> R.drawable.img_cat_fish
        rawName.contains("Egg", ignoreCase = true) -> R.drawable.img_cat_eggs
        rawName.contains("Ready", ignoreCase = true) -> R.drawable.img_cat_ready_to_cook
        rawName.contains("Organ", ignoreCase = true) -> R.drawable.img_cat_organ_meat
        rawName.contains("Sausage", ignoreCase = true) -> R.drawable.img_cat_sausages
        rawName.contains("Marinated", ignoreCase = true) -> R.drawable.img_cat_marinated
        else -> R.drawable.img_cat_chicken
    }
    val itemId = (data["categoryId"] as? String)?.takeIf { it.isNotBlank() } ?: rawName
    return QuickNavCategoryItem(
        id = itemId,
        label = rawName,
        imageRes = resolvedImageRes,
        isAllTab = isAllTab,
        isMoreTab = isMoreTab,
        order = order
    )
}

fun QuickNavCategoryItem.toFirestoreCategoryPayload(ownerId: String): Map<String, Any> = mapOf(
    "name" to label,
    "image" to (imageRes?.let { DrawableKeyMapper.toKey(it) } ?: ""),
    "isAllTab" to isAllTab,
    "isMoreTab" to isMoreTab,
    "order" to order.toLong(),
    "ownerId" to ownerId,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)


