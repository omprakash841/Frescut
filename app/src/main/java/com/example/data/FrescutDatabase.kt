package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FrescutDatabase private constructor() {

    private val productsState = MutableStateFlow<List<ProductEntity>>(SeedData.initialProducts)
    private val budgetConfigsState = MutableStateFlow<List<CategoryBudgetConfigEntity>>(SeedData.initialCategoryBudgetConfigs)
    private val recommendationConfigState = MutableStateFlow<RecommendationConfigEntity?>(SeedData.initialRecommendationConfig)
    private val cartItemsState = MutableStateFlow<List<CartItemEntity>>(emptyList())
    private val addressesState = MutableStateFlow<List<AddressEntity>>(emptyList())

    private val inMemoryDao = object : FrescutDao {
        override fun getAllProducts(): Flow<List<ProductEntity>> = productsState

        override suspend fun getProductCount(): Int = productsState.value.size

        override suspend fun insertProducts(products: List<ProductEntity>) {
            productsState.value = products
        }

        override suspend fun updateProductFavorite(productId: String, isFavorite: Boolean) {
            productsState.value = productsState.value.map {
                if (it.id == productId) it.copy(isFavorite = isFavorite) else it
            }
        }

        override fun getCategoryBudgetConfigs(): Flow<List<CategoryBudgetConfigEntity>> = budgetConfigsState

        override suspend fun getCategoryBudgetConfigCount(): Int = budgetConfigsState.value.size

        override suspend fun insertCategoryBudgetConfigs(configs: List<CategoryBudgetConfigEntity>) {
            budgetConfigsState.value = configs
        }

        override fun getRecommendationConfig(): Flow<RecommendationConfigEntity?> = recommendationConfigState

        override suspend fun getRecommendationConfigCount(): Int =
            if (recommendationConfigState.value != null) 1 else 0

        override suspend fun upsertRecommendationConfig(config: RecommendationConfigEntity) {
            recommendationConfigState.value = config
        }

        override fun getCartItems(): Flow<List<CartItemEntity>> = cartItemsState

        override suspend fun upsertCartItem(cartItem: CartItemEntity) {
            val current = cartItemsState.value.toMutableList()
            val idx = current.indexOfFirst { it.productId == cartItem.productId }
            if (idx >= 0) {
                current[idx] = cartItem
            } else {
                current.add(cartItem)
            }
            cartItemsState.value = current
        }

        override suspend fun deleteCartItem(productId: String) {
            cartItemsState.value = cartItemsState.value.filterNot { it.productId == productId }
        }

        override suspend fun clearCart() {
            cartItemsState.value = emptyList()
        }

        override suspend fun clearOrders() {
            // Orders are stored in Firestore
        }

        override suspend fun clearAddresses() {
            addressesState.value = emptyList()
        }

        override fun getAddresses(): Flow<List<AddressEntity>> =
            addressesState.map { list ->
                list.sortedWith(compareByDescending<AddressEntity> { it.isSelected }.thenBy { it.id })
            }

        override suspend fun getAddressCount(): Int = addressesState.value.size

        override suspend fun insertAddresses(addresses: List<AddressEntity>) {
            addressesState.value = addresses
        }

        override suspend fun insertAddress(address: AddressEntity) {
            val current = addressesState.value.map {
                if (address.isSelected) it.copy(isSelected = false) else it
            }.toMutableList()
            val nextId = if (address.id != 0) address.id else ((current.maxOfOrNull { it.id } ?: 0) + 1)
            val existingIdx = current.indexOfFirst { it.id == nextId }
            val entity = address.copy(id = nextId)
            if (existingIdx >= 0) {
                current[existingIdx] = entity
            } else {
                current.add(entity)
            }
            addressesState.value = current
        }

        override suspend fun selectAddress(selectedId: Int) {
            addressesState.value = addressesState.value.map {
                it.copy(isSelected = it.id == selectedId)
            }
        }
    }

    fun frescutDao(): FrescutDao = inMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: FrescutDatabase? = null

        @Volatile
        private var lastAppIdentityHash: Int = 0

        fun getDatabase(context: Context): FrescutDatabase {
            val appIdentity = System.identityHashCode(context.applicationContext)
            val existing = INSTANCE
            if (existing != null && lastAppIdentityHash == appIdentity) {
                return existing
            }
            return synchronized(this) {
                val current = INSTANCE
                if (current != null && lastAppIdentityHash == appIdentity) {
                    current
                } else {
                    val instance = FrescutDatabase()
                    INSTANCE = instance
                    lastAppIdentityHash = appIdentity
                    instance
                }
            }
        }
    }
}
