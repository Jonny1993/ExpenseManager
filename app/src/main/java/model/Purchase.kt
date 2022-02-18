package model

import androidx.room.*

@Entity(tableName = "Purchases",foreignKeys = [ForeignKey(entity = Category::class,
        parentColumns = arrayOf("cid"),
        childColumns = arrayOf("CategoryID"),
        onDelete = ForeignKey.SET_NULL)])

data class Purchase(
    @ColumnInfo(name = "Name") var name: String?,
    @ColumnInfo(name = "Price") var price: Float?,
    @ColumnInfo(name = "Date") var date: Long,
    @ColumnInfo(name = "Notes") var notes: String?,
    @ColumnInfo(name = "CategoryID") var categoryID: Int
){
    @PrimaryKey(autoGenerate = true)
    var pid: Int=0

}

@Entity(tableName = "Categories")
data class Category(
    @ColumnInfo(name = "categoryName") var categoryName: String
){
    @PrimaryKey(autoGenerate = true)
    var cid: Int=0

}