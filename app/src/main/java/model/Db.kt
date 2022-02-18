package model

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Purchase::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchasesDao
    abstract fun categoriesDao(): CategoriesDao
}
