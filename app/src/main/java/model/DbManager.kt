package model

import android.content.Context
import java.util.*

class DbManager(applicationContext:Context) {
    private val oneDay: Long = 86400000
    private var db = DbFactory.getDbContext(applicationContext)

    //Functions for purchases under this month (No month needed as argument)
    fun getAllPurchasesThisMonth(): List<Purchase> {
        return db!!.purchaseDao().getAllDuring(getFirstDateOfThisMonth(), getLastDateOfThisMonth())
    }

    fun getByTagForThisMonth(tagID: Int): List<Purchase> {
        return db!!.purchaseDao()
                .getByCategory(getFirstDateOfThisMonth(), getLastDateOfThisMonth(), tagID)
    }

    fun getSumForThisMonth(): Float {
        return db!!.purchaseDao().getSum(getFirstDateOfThisMonth(), getLastDateOfThisMonth())
    }

    fun getSumByTagForThisMonth(tagID: Int): Float {
        return db!!.purchaseDao()
                .getSumByCategory(getFirstDateOfThisMonth(), getLastDateOfThisMonth(), tagID)
    }

    fun getHighestSpentOnCategoriesThisMonth(): List<Category> {
        val cats = getListOfCategories()
        if(cats.size < 4) return cats
        val sortedCats = cats.sortedByDescending { getSumByTagForThisMonth(it.cid) }
        return sortedCats.subList(0,3)
    }

    //Functions for purchases under certain month (Month needed as argument)
    fun isCurrentMonth(value: Long): Boolean{
        return (value >= getFirstDateOfThisMonth() && value < getFirstDateOfMonth(1))
    }

    fun getAllPurchasesOneMonth(offset: Int): List<Purchase> {
        return db!!.purchaseDao()
                .getAllDuring(getFirstDateOfMonth(offset), getLastDateOfMonth(offset))
    }

    fun getByTagForOneMonth(tagID: Int, offset: Int): List<Purchase> {
        return db!!.purchaseDao()
                .getByCategory(getFirstDateOfMonth(offset), getLastDateOfMonth(offset), tagID)
    }

    fun getSumForOneMonth(offset: Int): Float {
        return db!!.purchaseDao().getSum(getFirstDateOfMonth(offset), getLastDateOfMonth(offset))
    }

    fun getSumByTagForOneMonth(tagID: Int, offset: Int): Float {
        return db!!.purchaseDao()
                .getSumByCategory(getFirstDateOfMonth(offset), getLastDateOfMonth(offset), tagID)
    }

    fun getHighestSpentOnCategoriesOneMonth(offset: Int): List<Category> {
        val cats = getListOfCategories()
        if(cats.size < 4) return cats
        val sortedCats = cats.sortedByDescending { getSumByTagForOneMonth(it.cid, offset) }
        return sortedCats.subList(0,3)
    }

    //Date independent
    fun addPurchase(purchase: Purchase) {
        return db!!.purchaseDao().insertPurchase(purchase)
    }

    fun deletePurchase(purchase: Purchase){
        return db!!.purchaseDao().delete(purchase)
    }

    fun getPurchaseById(id: Int): Purchase{
        return db!!.purchaseDao().findById(id)
    }


    //Functions for categories
    fun findCategory(categoryID: Int): Category {
        return db!!.categoriesDao().findCategory(categoryID)
    }

    fun updateCategory(category: Category) {
        return db!!.categoriesDao().updateCategory(category)
    }

    fun getListOfCategories(): List<Category> {
        return db!!.categoriesDao().getListOfCategories()
    }

    fun deleteCategory(category: Category) {
        return db!!.categoriesDao().delete(category)
    }

    fun addCategory(category: Category) {
        return db!!.categoriesDao().addCategory(category)
    }

    //***Help functions****//
    //Offset relative to now in months, 12 = a year ahead, -12 =year back in time etc
    private fun getFirstDateOfMonth(offset: Int): Long {

        val offsetDate = extractDateFromOffset(offset)
        offsetDate.set(Calendar.DAY_OF_MONTH, 1)
        return offsetDate.timeInMillis-oneDay
    }

    //Same as above
    private fun getLastDateOfMonth(offset: Int): Long {

        val offsetDate = extractDateFromOffset(offset)
        val lastDayOfThatMonth =
                offsetDate.getActualMaximum(Calendar.DAY_OF_MONTH)//Returns 31/30/28 or 29
        offsetDate.set(Calendar.DAY_OF_MONTH, lastDayOfThatMonth)
        return offsetDate.timeInMillis

    }

    private fun extractDateFromOffset(offset: Int): Calendar {

        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)
        val yearOffset = offset / 12
        val monthOffset = offset % 12
        val offsetDate = Calendar.getInstance()
        offsetDate.set(Calendar.MONTH, thisMonth + monthOffset)
        offsetDate.set(Calendar.YEAR, thisYear + yearOffset)
        return offsetDate
    }

    private fun getFirstDateOfThisMonth(): Long {
        val now = Calendar.getInstance()
        now.set((Calendar.DAY_OF_MONTH), 1)//No longer exactly "now"
        return now.timeInMillis-oneDay
    }

    private fun getLastDateOfThisMonth(): Long {
        val now = Calendar.getInstance()
        val lastDayOfThisMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)//Returns 31/30/28 or 29

        val lastDateOfThisMonth = Calendar.getInstance()//Returns now
        lastDateOfThisMonth.set(Calendar.DAY_OF_MONTH, lastDayOfThisMonth)

        return lastDateOfThisMonth.timeInMillis

    }
}