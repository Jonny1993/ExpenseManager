package model

import androidx.room.*
import model.Purchase

@Dao
interface  PurchasesDao {

    @Query("SELECT * FROM purchases WHERE CategoryID=:id")
    fun findById(id: Int): Purchase
    @Query("SELECT * FROM purchases WHERE Date < :end AND Date > :start ORDER BY Date")
    fun getAllDuring(start: Long,end:Long): List<Purchase>
    @Query("SELECT * FROM purchases ORDER BY Date")
    fun getAll(): List<Purchase>
    @Query("SELECT * FROM purchases WHERE Date < :end AND Date > :start AND CategoryID=:categoryID ORDER BY Date")
    fun getByCategory(start: Long,end:Long,categoryID:Int): List<Purchase>

    @Query("SELECT SUM(Price) FROM purchases WHERE Date < :end AND Date > :start")
    fun getSum(start: Long,end:Long): Float
    @Query("SELECT SUM(Price) FROM purchases WHERE Date < :end AND Date > :start AND CategoryID=:categoryID")
    fun getSumByCategory(start: Long,end:Long,categoryID:Int): Float

    @Insert
    fun insertPurchase(purchase: Purchase)


    @Update
    fun updatePurchase(purchase: Purchase)

/*
    @Insert
    fun insertTags(vararg tags: Tag)
*/
    @Delete
    fun delete(purchase: Purchase)

}