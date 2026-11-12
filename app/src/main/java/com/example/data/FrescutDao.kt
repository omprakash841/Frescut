package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FrescutDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateProductFavorite(productId: String, isFavorite: Boolean)

    @Query("UPDATE products SET pricePerKg = :pricePerKg, budgetSelectorEnabled = :enabled WHERE id = :productId")
    suspend fun updateProductBudgetSettings(productId: String, pricePerKg: Int, enabled: Boolean)

    @Query("SELECT * FROM category_budget_configs")
    fun getCategoryBudgetConfigs(): Flow<List<CategoryBudgetConfigEntity>>

    @Query("SELECT COUNT(*) FROM category_budget_configs")
    suspend fun getCategoryBudgetConfigCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudgetConfigs(configs: List<CategoryBudgetConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudgetConfig(config: CategoryBudgetConfigEntity)

    @Query("SELECT * FROM recommendation_configs WHERE id = 1")
    fun getRecommendationConfig(): Flow<RecommendationConfigEntity?>

    @Query("SELECT COUNT(*) FROM recommendation_configs")
    suspend fun getRecommendationConfigCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecommendationConfig(config: RecommendationConfigEntity)

    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM orders ORDER BY createdAtEpoch DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM addresses ORDER BY isSelected DESC, id ASC")
    fun getAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT COUNT(*) FROM addresses")
    suspend fun getAddressCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isSelected = (id = :selectedId)")
    suspend fun selectAddress(selectedId: Int)
}
