package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FrescutRepository(
    private val db: FirebaseFirestore,
    private val dao: FrescutDao? = null,
    private val sessionUserId: String? = null
) {
    constructor(
        context: Context,
        dao: FrescutDao? = null,
        sessionUserId: String? = null
    ) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        dao,
        sessionUserId
    )

    private val auth = Firebase.auth
    private val localFavorites = MutableStateFlow<Set<String>>(emptySet())

    private fun effectiveUserIdOrNull(): String? {
        return auth.currentUser?.uid ?: sessionUserId?.takeIf { it.isNotBlank() }
    }

    private fun requireUserId(): String {
        return effectiveUserIdOrNull()
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    val cartItemsFlow: Flow<List<CartItemEntity>> = dao?.getCartItems() ?: flowOf(emptyList())

    val categoryBudgetConfigsFlow: Flow<List<CategoryBudgetConfigEntity>> =
        dao?.getCategoryBudgetConfigs() ?: flowOf(SeedData.initialCategoryBudgetConfigs)

    val recommendationConfigFlow: Flow<RecommendationConfigEntity> =
        dao?.getRecommendationConfig()?.map { it ?: SeedData.initialRecommendationConfig }
            ?: flowOf(SeedData.initialRecommendationConfig)

    // Live Products from Cloud Firestore, backed by Room offline cache
    fun observeProducts(): Flow<List<ProductEntity>> = flow {
        val uid = effectiveUserIdOrNull()
        if (uid == null) {
            if (dao != null) {
                emitAll(dao.getAllProducts())
            } else {
                emit(SeedData.initialProducts)
            }
            return@flow
        }

        val path = "products"
        val firestoreProductsFlow = db.collection(path)
            .whereEqualTo("available", true)
            .snapshots()
            .map { snapshot ->
                val docs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(
                        FirestoreProduct::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                if (docs.isNotEmpty()) {
                    val entities = docs.map { it.toProductEntity(isFavorite = false) }
                    dao?.insertProducts(entities)
                    entities
                } else {
                    SeedData.initialProducts
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && effectiveUserIdOrNull() == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                if (dao != null) {
                    emitAll(dao.getAllProducts())
                } else {
                    throw error
                }
            }

        emitAll(
            combine(firestoreProductsFlow, localFavorites) { products, favIds ->
                products.map { prod ->
                    prod.copy(isFavorite = favIds.contains(prod.id) || prod.isFavorite)
                }
            }
        )
    }

    val productsFlow: Flow<List<ProductEntity>>
        get() = observeProducts()

    val cartProductsFlow: Flow<List<CartProductItem>>
        get() = combine(
            observeProducts(),
            cartItemsFlow
        ) { products, cartItems ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                val product = productMap[item.productId]
                if (product != null && item.quantity > 0) {
                    CartProductItem(
                        product = product,
                        quantity = item.quantity,
                        selectedBudget = item.selectedBudget,
                        estimatedGrams = item.estimatedGrams
                    )
                } else {
                    null
                }
            }
        }

    // Live Orders read strictly from Cloud Firestore (orders where userId == current user uid)
    fun observeUserOrders(): Flow<List<OrderDomainModel>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    val sortedDocs = snapshot.documents.sortedByDescending { doc ->
                        doc.getTimestamp(
                            "createdAt",
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ) ?: Timestamp(0, 0)
                    }
                    sortedDocs.mapNotNull { doc -> doc.toOrderDomainModel() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && effectiveUserIdOrNull() == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    val ordersFlow: Flow<List<OrderDomainModel>>
        get() = observeUserOrders()

    // Live User Addresses from Cloud Firestore (/users/{uid}/addresses)
    fun observeUserAddresses(): Flow<List<AddressEntity>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/addresses"
        emitAll(
            db.collection("users")
                .document(uid)
                .collection("addresses")
                .snapshots()
                .map { snapshot ->
                    val docs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(
                            FirestoreUserAddress::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }.filter { it.fullAddress.isNotBlank() }

                    if (docs.isEmpty()) {
                        emptyList()
                    } else {
                        docs.mapIndexed { idx, addr ->
                            val numericId = addr.id.removePrefix("addr_").toLongOrNull()?.let { (it % 1000000L).toInt() }
                                ?: (idx + 1)
                            AddressEntity(
                                id = numericId,
                                label = addr.label.ifBlank { "Home" },
                                areaName = addr.areaName,
                                fullAddress = addr.fullAddress,
                                houseNumber = addr.houseNumber,
                                floor = addr.floor,
                                landmark = addr.landmark,
                                receiverPhone = addr.receiverPhone,
                                latitude = addr.latitude,
                                longitude = addr.longitude,
                                deliveryMins = addr.deliveryMins.toInt(),
                                isSelected = addr.isSelected
                            )
                        }.sortedWith(compareByDescending<AddressEntity> { it.isSelected }.thenBy { it.id })
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && effectiveUserIdOrNull() == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    if (dao != null) {
                        emit(emptyList())
                    } else {
                        throw error
                    }
                }
        )
    }

    val addressesFlow: Flow<List<AddressEntity>>
        get() = observeUserAddresses()

    // Live Active Coupons from Cloud Firestore (/coupons where active == true)
    fun observeActiveCoupons(): Flow<List<CouponOfferModel>> = flow {
        val path = "coupons"
        emitAll(
            db.collection(path)
                .whereEqualTo("active", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> doc.toCouponOfferModel() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    // Live Promotional Banners from Cloud Firestore (/banners - only active banners sorted by order)
    fun observeBanners(): Flow<List<PromotionalBannerModel>> = flow {
        val path = "banners"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .mapNotNull { doc -> doc.toPromotionalBannerModel() }
                        .filter { it.active }
                        .sortedBy { it.order }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    // Live Recipes from Cloud Firestore (/recipes - only active recipes sorted by order)
    fun observeRecipes(): Flow<List<RecipeCardModel>> = flow {
        val path = "recipes"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .mapNotNull { doc -> doc.toRecipeCardModel() }
                        .filter { it.active }
                        .sortedBy { it.order }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    // Live Customer Reviews from Cloud Firestore (/reviews - only active reviews sorted by order)
    fun observeReviews(): Flow<List<CustomerReviewModel>> = flow {
        val path = "reviews"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .mapNotNull { doc -> doc.toCustomerReviewModel() }
                        .filter { it.active }
                        .sortedBy { it.order }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    // Live App Config (config/app_config) from Cloud Firestore (/config)
    fun observeAppConfig(): Flow<AppConfigModel> = flow {
        val path = "config"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    val primaryDoc = snapshot.documents.firstOrNull { it.id == "app_config" }
                        ?: snapshot.documents.firstOrNull()
                    if (primaryDoc != null) {
                        val parsed = primaryDoc.toAppConfigModel()
                        val allConfigs = snapshot.documents.map { it.toAppConfigModel() }
                        AppConfigModel(
                            referralCode = parsed.referralCode.ifBlank {
                                allConfigs.firstOrNull { it.referralCode.isNotBlank() }?.referralCode ?: ""
                            },
                            searchChips = parsed.searchChips.ifEmpty {
                                allConfigs.firstOrNull { it.searchChips.isNotEmpty() }?.searchChips ?: emptyList()
                            },
                            storeName = parsed.storeName,
                            supportPhone = parsed.supportPhone,
                            minOrderAmount = parsed.minOrderAmount,
                            deliveryTimeText = parsed.deliveryTimeText.ifBlank {
                                allConfigs.firstOrNull { it.deliveryTimeText.isNotBlank() }?.deliveryTimeText ?: ""
                            },
                            deliveryFee = parsed.deliveryFee ?: allConfigs.firstNotNullOfOrNull { it.deliveryFee },
                            handlingFee = parsed.handlingFee ?: allConfigs.firstNotNullOfOrNull { it.handlingFee },
                            storeLatitude = parsed.storeLatitude,
                            storeLongitude = parsed.storeLongitude,
                            deliveryRadiusKm = parsed.deliveryRadiusKm
                        )
                    } else {
                        AppConfigModel()
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(AppConfigModel())
                }
        )
    }

    // Live Quick Categories Row from Cloud Firestore (/categories)
    fun observeQuickCategories(): Flow<List<QuickNavCategoryItem>> = flow {
        val path = "categories"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    val items = snapshot.documents
                        .mapNotNull { doc -> doc.toQuickNavCategoryItem() }
                        .sortedBy { it.order }
                    if (items.isNotEmpty()) {
                        items
                    } else {
                        SeedData.defaultQuickCategories
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    emit(SeedData.defaultQuickCategories)
                }
        )
    }

    fun observeUserProfile(): Flow<FirestoreUserProfile?> = flow {
        val currentUid = effectiveUserIdOrNull()
        if (currentUid == null) {
            emit(null)
            return@flow
        }
        val path = "users/$currentUid"
        emitAll(
            db.collection("users")
                .document(currentUid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            FirestoreUserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && effectiveUserIdOrNull() == currentUid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    // One-time cloud seeding of collections ONLY when each collection is empty (reviews collection is intentionally NOT seeded)
    suspend fun ensureSeeded() {
        // 1. Seed Room offline cache for products/configs only, and clear any legacy local orders & addresses
        if (dao != null) {
            dao.clearOrders()
            dao.clearAddresses()
            if (dao.getProductCount() == 0) {
                dao.insertProducts(SeedData.initialProducts)
            }
            if (dao.getCategoryBudgetConfigCount() == 0) {
                dao.insertCategoryBudgetConfigs(SeedData.initialCategoryBudgetConfigs)
            }
            if (dao.getRecommendationConfigCount() == 0) {
                dao.upsertRecommendationConfig(SeedData.initialRecommendationConfig)
            }
        }

        val uid = effectiveUserIdOrNull() ?: return
        val currentUser = auth.currentUser

        // 2. Upload SeedData products to Firestore ONLY once when the products collection is completely empty
        val productsPath = "products"
        try {
            val existingProducts = db.collection(productsPath)
                .whereEqualTo("available", true)
                .limit(1)
                .get()
                .await()

            if (existingProducts.isEmpty) {
                for (product in SeedData.initialProducts) {
                    val docRef = db.collection(productsPath).document(product.id)
                    docRef.set(product.toFirestorePayload(ownerId = uid)).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, productsPath)
        }

        // 2b. Upload Quick Categories (All, Chicken, Mutton, Fish, Eggs, Ready to Cook, More) ONLY once when categories collection is empty
        val categoriesPath = "categories"
        try {
            val existingCategories = db.collection(categoriesPath)
                .limit(1)
                .get()
                .await()

            if (existingCategories.isEmpty) {
                for (cat in SeedData.defaultQuickCategories) {
                    val docId = "cat_${cat.id.lowercase().replace(Regex("[^a-z0-9]+"), "_")}"
                    val docRef = db.collection(categoriesPath).document(docId)
                    docRef.set(cat.toFirestoreCategoryPayload(ownerId = uid)).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, categoriesPath)
        }

        // 2c. Seed config/app_config ONLY when config collection is empty (or backfill missing radius/store coordinates)
        val configPath = "config"
        try {
            val existingConfig = db.collection(configPath)
                .limit(1)
                .get()
                .await()

            if (existingConfig.isEmpty) {
                db.collection(configPath)
                    .document("app_config")
                    .set(SeedData.initialAppConfigSeed.toFirestoreAppConfigPayload(ownerId = uid))
                    .await()
            } else {
                val appConfigDoc = db.collection(configPath).document("app_config").get().await()
                if (appConfigDoc.exists() && appConfigDoc.getString("ownerId") == uid) {
                    val missingUpdates = mutableMapOf<String, Any>()
                    if (!appConfigDoc.contains("storeLatitude")) missingUpdates["storeLatitude"] = 0.0
                    if (!appConfigDoc.contains("storeLongitude")) missingUpdates["storeLongitude"] = 0.0
                    if (!appConfigDoc.contains("deliveryRadiusKm")) missingUpdates["deliveryRadiusKm"] = 0.0
                    if (missingUpdates.isNotEmpty()) {
                        missingUpdates["updatedAt"] = FieldValue.serverTimestamp()
                        db.collection(configPath).document("app_config").update(missingUpdates).await()
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, configPath)
        }

        // 2d. Seed banners collection (2 documents: "Fresh Meat & Fish", "Weekend Combo Packs") ONLY when banners collection is empty
        val bannersPath = "banners"
        try {
            val existingBanners = db.collection(bannersPath)
                .limit(1)
                .get()
                .await()

            if (existingBanners.isEmpty) {
                for (banner in SeedData.initialBannersSeed) {
                    db.collection(bannersPath)
                        .document(banner.id)
                        .set(banner.toFirestoreBannerPayload(ownerId = uid))
                        .await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, bannersPath)
        }

        // 2e. Seed coupons collection (1 document: code "WELCOME10", discountPercent 10, minOrder 0, active false) ONLY when coupons collection is empty
        val couponsPath = "coupons"
        try {
            val existingCoupons = db.collection(couponsPath)
                .limit(1)
                .get()
                .await()

            if (existingCoupons.isEmpty) {
                for (coupon in SeedData.initialCouponsSeed) {
                    db.collection(couponsPath)
                        .document(coupon.code)
                        .set(coupon.toFirestoreCouponPayload(ownerId = uid))
                        .await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, couponsPath)
        }

        // 2f. Seed recipes collection (3 documents: "Butter Chicken", "Mutton Rogan Josh", "Garlic Prawns") ONLY when recipes collection is empty
        val recipesPath = "recipes"
        try {
            val existingRecipes = db.collection(recipesPath)
                .limit(1)
                .get()
                .await()

            if (existingRecipes.isEmpty) {
                for (recipe in SeedData.initialRecipesSeed) {
                    db.collection(recipesPath)
                        .document(recipe.id)
                        .set(recipe.toFirestoreRecipePayload(ownerId = uid))
                        .await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, recipesPath)
        }

        // NOTE: reviews collection is intentionally NOT seeded so that it remains empty until a review is added.

        // 3. Initialize UserProfile document in /users/{uid} with empty phone & empty deliveryInstructions
        val userDocPath = "users/$uid"
        try {
            val userDocRef = db.collection("users").document(uid)
            val userDoc = userDocRef.get().await()
            if (!userDoc.exists()) {
                val defaultName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "FRESCUT Customer"
                val defaultEmail = currentUser?.email ?: ""
                val defaultPhone = currentUser?.phoneNumber ?: ""
                val profilePayload = mapOf(
                    "userId" to uid,
                    "name" to defaultName,
                    "email" to defaultEmail,
                    "phone" to defaultPhone,
                    "isProMember" to false,
                    "selectedPaymentMethod" to "Cash on Delivery",
                    "deliveryInstructions" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(profilePayload).await()
            } else {
                // Clean up any legacy seeded phone or delivery note from earlier versions
                val existingPhone = userDoc.getString("phone") ?: ""
                val existingNote = userDoc.getString("deliveryInstructions") ?: ""
                val updates = mutableMapOf<String, Any>()
                if (existingPhone.contains("98610") && existingPhone.contains("45210")) {
                    updates["phone"] = ""
                }
                if (existingNote.startsWith("Ring bell once, hand over cold-chain")) {
                    updates["deliveryInstructions"] = ""
                }
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    userDocRef.update(updates).await()
                }

            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocPath)
        }

        // 4. Clean up any legacy seeded demo addresses (addr_1, addr_2) from earlier runs so new/existing users have no fake addresses
        val addressesPath = "users/$uid/addresses"
        try {
            val colRef = db.collection("users").document(uid).collection("addresses")
            for (legacyDocId in listOf("addr_1", "addr_2")) {
                val docSnap = colRef.document(legacyDocId).get().await()
                if (docSnap.exists()) {
                    colRef.document(legacyDocId).delete().await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, addressesPath)
        }
    }

    // Product CRUD methods (for initial seed and rule verification)
    suspend fun seedProduct(product: ProductEntity) {
        val uid = requireUserId()
        val docRef = db.collection("products").document(product.id)
        try {
            docRef.set(product.toFirestorePayload(ownerId = uid)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getAvailableProducts(): List<ProductEntity> {
        val path = "products"
        return try {
            val snapshot = db.collection(path)
                .whereEqualTo("available", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(
                    FirestoreProduct::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.toProductEntity()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun updateProductPricePerKg(productId: String, newPricePerKg: Int) {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.update(
                mapOf(
                    "pricePerKg" to newPricePerKg.toLong(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProduct(productId: String) {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun toggleFavorite(product: ProductEntity) {
        val current = localFavorites.value.toMutableSet()
        val nextFav = !product.isFavorite
        if (nextFav) {
            current.add(product.id)
        } else {
            current.remove(product.id)
        }
        localFavorites.value = current
        dao?.updateProductFavorite(product.id, nextFav)
    }

    suspend fun setCartQuantity(
        productId: String,
        newQuantity: Int,
        selectedBudget: Int? = null,
        estimatedGrams: Int? = null
    ) {
        if (newQuantity <= 0) {
            dao?.deleteCartItem(productId)
        } else {
            dao?.upsertCartItem(
                CartItemEntity(
                    productId = productId,
                    quantity = newQuantity,
                    selectedBudget = selectedBudget,
                    estimatedGrams = estimatedGrams
                )
            )
        }
    }

    suspend fun clearCart() {
        dao?.clearCart()
    }

    suspend fun updateUserProfileInCloud(
        name: String,
        phone: String,
        email: String,
        isProMember: Boolean,
        selectedPaymentMethod: String,
        deliveryInstructions: String
    ) {
        val currentUid = effectiveUserIdOrNull() ?: return
        val docRef = db.collection("users").document(currentUid)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "name" to name.take(100),
                        "phone" to phone.take(30),
                        "email" to email.take(150),
                        "isProMember" to isProMember,
                        "selectedPaymentMethod" to selectedPaymentMethod.take(100),
                        "deliveryInstructions" to deliveryInstructions.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to currentUid,
                        "name" to name.take(100),
                        "phone" to phone.take(30),
                        "email" to email.take(150),
                        "isProMember" to isProMember,
                        "selectedPaymentMethod" to selectedPaymentMethod.take(100),
                        "deliveryInstructions" to deliveryInstructions.take(300),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun selectAddress(addressId: Int) {
        dao?.selectAddress(addressId)
        val uid = effectiveUserIdOrNull() ?: return
        val colRef = db.collection("users").document(uid).collection("addresses")
        try {
            val allDocs = colRef.get().await()
            for (doc in allDocs.documents) {
                val docNum = doc.id.removePrefix("addr_").toLongOrNull()?.let { (it % 1000000L).toInt() }
                val isTarget = doc.id == "addr_$addressId" || docNum == addressId
                doc.reference.update(
                    mapOf(
                        "isSelected" to isTarget,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, colRef.path)
        }
    }

    suspend fun addNewAddress(
        label: String,
        areaName: String,
        fullAddress: String,
        houseNumber: String = "",
        floor: String = "",
        landmark: String = "",
        receiverPhone: String = "",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val safeLabel = label.trim().ifBlank { "Home" }.take(60)
        val safeArea = areaName.trim().take(120)
        val safeFull = fullAddress.trim().take(400)
        val safeHouse = houseNumber.trim().take(200)
        val safeFloor = floor.trim().take(80)
        val safeLandmark = landmark.trim().take(160)
        val safePhone = receiverPhone.trim().take(30)
        require(safeArea.isNotBlank() && safeFull.isNotBlank()) {
            "Area name and full address are required."
        }

        dao?.insertAddress(
            AddressEntity(
                label = safeLabel,
                areaName = safeArea,
                fullAddress = safeFull,
                houseNumber = safeHouse,
                floor = safeFloor,
                landmark = safeLandmark,
                receiverPhone = safePhone,
                latitude = latitude,
                longitude = longitude,
                deliveryMins = 30,
                isSelected = true
            )
        )

        val currentUid = effectiveUserIdOrNull() ?: return
        val colRef = db.collection("users").document(currentUid).collection("addresses")
        val docId = "addr_${System.currentTimeMillis()}"
        val docRef = colRef.document(docId)
        try {
            val existingDocs = colRef.get().await()
            for (doc in existingDocs.documents) {
                if (doc.getBoolean("isSelected") == true) {
                    doc.reference.update(
                        mapOf(
                            "isSelected" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            val addressPayload = mutableMapOf<String, Any>(
                "userId" to currentUid,
                "label" to safeLabel,
                "areaName" to safeArea,
                "fullAddress" to safeFull,
                "houseNumber" to safeHouse,
                "floor" to safeFloor,
                "landmark" to safeLandmark,
                "receiverPhone" to safePhone,
                "deliveryMins" to 30L,
                "isSelected" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (latitude != null && longitude != null) {
                addressPayload["latitude"] = latitude
                addressPayload["longitude"] = longitude
            }
            docRef.set(addressPayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun placeOrder(
        cartItems: List<CartProductItem>,
        discountAmount: Int,
        deliveryFee: Int,
        handlingFee: Int = 0,
        addressText: String,
        houseNumber: String = "",
        floor: String = "",
        landmark: String = "",
        receiverPhone: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        deliveryInstructions: String = "",
        paymentMethod: String = "Cash on Delivery"
    ): String {
        if (cartItems.isEmpty()) return ""
        val cleanAddress = addressText.trim().take(400)
        require(cleanAddress.isNotBlank()) { "Delivery address is required to place an order." }
        val uid = requireUserId()

        val indiaTimeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        val nowDate = Date()
        val ddmmyyFormat = SimpleDateFormat("ddMMyy", Locale.US).apply {
            timeZone = indiaTimeZone
        }
        val ddmmyy = ddmmyyFormat.format(nowDate)
        val nowTime = SimpleDateFormat("h:mm a", Locale.US).format(nowDate)
        val placedTimeFormatted = SimpleDateFormat("d MMM, h:mm a", Locale.US).format(nowDate)
        val itemSubtotal = cartItems.sumOf { it.lineTotal }
        val totalPaid = (itemSubtotal - discountAmount + deliveryFee + handlingFee).coerceAtLeast(0)

        val itemsList: List<Map<String, Any>> = cartItems.map { item ->
            val weightDesc = if (item.isCustomBudget && item.estimatedGrams != null) {
                "~${item.estimatedGrams} g (Budget ₹${item.selectedBudget})"
            } else {
                item.product.weightInfo
            }
            mapOf(
                "productId" to item.product.id,
                "name" to item.product.name,
                "weight" to weightDesc,
                "price" to item.unitEffectivePrice.toLong(),
                "quantity" to item.quantity.toLong(),
                "image" to DrawableKeyMapper.toKey(item.product.imageRes)
            )
        }

        val counterDocId = "orders_$ddmmyy"
        val counterRef = db.collection("counters").document(counterDocId)

        return try {
            val rawOrderNumber = db.runTransaction { transaction ->
                val counterSnap = transaction.get(counterRef)
                val currentCount = if (counterSnap.exists()) {
                    counterSnap.getLong("count") ?: 0L
                } else {
                    0L
                }
                val nextSeq = currentCount + 1L
                val generatedOrderNumber = String.format(Locale.US, "FC-%s-%04d", ddmmyy, nextSeq)
                val orderRef = db.collection("orders").document(generatedOrderNumber)

                val counterPayload = mapOf(
                    "count" to nextSeq,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                transaction.set(counterRef, counterPayload)

                // Both document ID and orderNumber field are stored without '#' (e.g. FC-151125-0001)
                val payload = mutableMapOf<String, Any>(
                    "orderNumber" to generatedOrderNumber,
                    "userId" to uid,
                    "items" to itemsList,
                    "total" to totalPaid.toLong(),
                    "itemTotal" to itemSubtotal.toLong(),
                    "deliveryFee" to deliveryFee.toLong(),
                    "packagingCharge" to handlingFee.toLong(),
                    "address" to cleanAddress,
                    "houseNumber" to houseNumber.trim().take(200),
                    "floor" to floor.trim().take(80),
                    "landmark" to landmark.trim().take(160),
                    "receiverPhone" to receiverPhone.trim().take(30),
                    "deliveryInstructions" to deliveryInstructions.trim().take(300),
                    "paymentMethod" to paymentMethod.ifBlank { "Cash on Delivery" }.take(100),
                    "status" to "Order Placed",
                    "time" to placedTimeFormatted,
                    "timelineTimes" to listOf(nowTime, "", "", "", ""),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (latitude != null && longitude != null) {
                    payload["latitude"] = latitude
                    payload["longitude"] = longitude
                }

                transaction.set(orderRef, payload)
                generatedOrderNumber
            }.await()

            dao?.clearCart()
            "#$rawOrderNumber"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "orders")
            throw e
        }
    }

    suspend fun rateOrder(orderId: String, ratingValue: Int) {
        requireUserId()
        val cleanDocId = orderId.removePrefix("#")
        val docRef = db.collection("orders").document(cleanDocId)
        try {
            docRef.update(
                mapOf(
                    "rating" to ratingValue.coerceIn(1, 5).toDouble(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun getOrderById(orderDocId: String): OrderDomainModel? {
        val cleanDocId = orderDocId.removePrefix("#")
        val docRef = db.collection("orders").document(cleanDocId)
        return try {
            val snap = docRef.get().await()
            snap.toOrderDomainModel()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun reorderItems(order: OrderDomainModel) {
        order.items.forEach { line ->
            dao?.upsertCartItem(CartItemEntity(productId = line.productId, quantity = line.quantity))
        }
    }
}

