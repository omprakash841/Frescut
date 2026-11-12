package com.example.data

import com.example.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FrescutRepository(private val dao: FrescutDao) {

    val productsFlow: Flow<List<ProductEntity>> = dao.getAllProducts()
    val cartItemsFlow: Flow<List<CartItemEntity>> = dao.getCartItems()
    val addressesFlow: Flow<List<AddressEntity>> = dao.getAddresses()
    val categoryBudgetConfigsFlow: Flow<List<CategoryBudgetConfigEntity>> = dao.getCategoryBudgetConfigs()
    val recommendationConfigFlow: Flow<RecommendationConfigEntity> = dao.getRecommendationConfig().map {
        it ?: SeedData.initialRecommendationConfig
    }

    val cartProductsFlow: Flow<List<CartProductItem>> = combine(
        productsFlow,
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

    val ordersFlow: Flow<List<OrderDomainModel>> = dao.getAllOrders().map { entities ->
        entities.map { entity -> entity.toDomainModel() }
    }

    suspend fun ensureSeeded() {
        if (dao.getProductCount() == 0) {
            dao.insertProducts(SeedData.initialProducts)
        }
        if (dao.getCategoryBudgetConfigCount() == 0) {
            dao.insertCategoryBudgetConfigs(SeedData.initialCategoryBudgetConfigs)
        }
        if (dao.getRecommendationConfigCount() == 0) {
            dao.upsertRecommendationConfig(SeedData.initialRecommendationConfig)
        }
        if (dao.getOrderCount() == 0) {
            dao.insertOrders(SeedData.initialOrders)
        }
        if (dao.getAddressCount() == 0) {
            dao.insertAddresses(SeedData.initialAddresses)
        }
    }

    suspend fun toggleFavorite(product: ProductEntity) {
        dao.updateProductFavorite(product.id, !product.isFavorite)
    }

    suspend fun setCartQuantity(
        productId: String,
        newQuantity: Int,
        selectedBudget: Int? = null,
        estimatedGrams: Int? = null
    ) {
        if (newQuantity <= 0) {
            dao.deleteCartItem(productId)
        } else {
            dao.upsertCartItem(
                CartItemEntity(
                    productId = productId,
                    quantity = newQuantity,
                    selectedBudget = selectedBudget,
                    estimatedGrams = estimatedGrams
                )
            )
        }
    }

    suspend fun updateCategoryBudgetConfig(config: CategoryBudgetConfigEntity) {
        dao.upsertCategoryBudgetConfig(config)
    }

    suspend fun updateRecommendationConfig(config: RecommendationConfigEntity) {
        dao.upsertRecommendationConfig(config)
    }

    suspend fun updateProductBudgetSettings(productId: String, pricePerKg: Int, enabled: Boolean) {
        dao.updateProductBudgetSettings(productId, pricePerKg, enabled)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    suspend fun selectAddress(addressId: Int) {
        dao.selectAddress(addressId)
    }

    suspend fun addNewAddress(label: String, areaName: String, fullAddress: String) {
        val inserted = AddressEntity(
            label = label.ifBlank { "Home" },
            areaName = areaName.ifBlank { "Udhepali, Bargarh" },
            fullAddress = fullAddress,
            deliveryMins = 30,
            isSelected = true
        )
        dao.insertAddress(inserted)
    }

    suspend fun placeOrder(
        cartItems: List<CartProductItem>,
        discountAmount: Int,
        deliveryFee: Int
    ): String {
        if (cartItems.isEmpty()) return ""
        val orderNum = (12848..12999).random()
        val newOrderId = "#FC$orderNum"
        val nowTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val itemSubtotal = cartItems.sumOf { it.lineTotal }
        val totalPaid = (itemSubtotal - discountAmount + deliveryFee).coerceAtLeast(0)

        val encodedItems = cartItems.joinToString("||") { item ->
            val weightDesc = if (item.isCustomBudget && item.estimatedGrams != null) {
                "~${item.estimatedGrams} g (Budget ₹${item.selectedBudget})"
            } else {
                item.product.weightInfo
            }
            "${item.product.id}::${item.product.name}::$weightDesc::${item.unitEffectivePrice}::${item.quantity}::${item.product.imageRes}"
        }

        val orderEntity = OrderEntity(
            orderId = newOrderId,
            status = "Out for Delivery",
            placedAtText = "Today, $nowTime",
            etaText = "Arriving in ~ 24 min",
            itemTotal = itemSubtotal,
            deliveryFee = deliveryFee,
            packagingCharge = 0,
            totalAmount = totalPaid,
            rating = 5.0f,
            itemsEncoded = encodedItems,
            timelineEncoded = "$nowTime,$nowTime,$nowTime,$nowTime,",
            createdAtEpoch = System.currentTimeMillis()
        )
        dao.insertOrder(orderEntity)
        dao.clearCart()
        return newOrderId
    }

    suspend fun reorderItems(order: OrderDomainModel) {
        order.items.forEach { line ->
            dao.upsertCartItem(CartItemEntity(productId = line.productId, quantity = line.quantity))
        }
    }

    private fun OrderEntity.toDomainModel(): OrderDomainModel {
        val parsedItems = itemsEncoded.split("||").mapNotNull { token ->
            val parts = token.split("::")
            if (parts.size >= 6) {
                OrderLineItem(
                    productId = parts[0],
                    name = parts[1],
                    weight = parts[2],
                    price = parts[3].toIntOrNull() ?: 0,
                    quantity = parts[4].toIntOrNull() ?: 1,
                    imageRes = parts[5].toIntOrNull() ?: R.drawable.img_prod_chicken_breast
                )
            } else {
                null
            }
        }
        val times = timelineEncoded.split(",")
        val paddedTimes = List(5) { idx -> times.getOrNull(idx) ?: "" }
        return OrderDomainModel(
            orderId = orderId,
            status = status,
            placedAtText = placedAtText,
            etaText = etaText,
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            packagingCharge = packagingCharge,
            totalAmount = totalAmount,
            rating = rating,
            items = parsedItems,
            timelineTimes = paddedTimes
        )
    }
}
