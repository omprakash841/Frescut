package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        AddressEntity::class,
        CategoryBudgetConfigEntity::class,
        RecommendationConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FrescutDatabase : RoomDatabase() {
    abstract fun frescutDao(): FrescutDao

    companion object {
        @Volatile
        private var INSTANCE: FrescutDatabase? = null

        fun getDatabase(context: Context): FrescutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrescutDatabase::class.java,
                    "frescut_store_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
