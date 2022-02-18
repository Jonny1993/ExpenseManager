package model

import androidx.room.*

@Dao
interface CategoriesDao {
    @Query("SELECT *  FROM Categories WHERE cid=:categoryID")
    fun findCategory(categoryID:Int): Category

    @Update
    fun updateCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getListOfCategories(): List<Category>


    @Delete
    fun delete(category: Category)


    @Insert
    fun addCategory(category: Category)
}
