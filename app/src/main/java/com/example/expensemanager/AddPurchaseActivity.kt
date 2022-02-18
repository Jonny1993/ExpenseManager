package com.example.expensemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import model.DbManager
import model.Category
import model.Purchase
import uiutils.MsgUtils
import java.util.*


class AddPurchaseActivity : AppCompatActivity() {
    private lateinit var model: DbManager
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val categories = ArrayList<Category>()
    private val categoryStrings = ArrayList<String>()
    private lateinit var dateSelector: CalendarView
    private val calendar = Calendar.getInstance()
    private var dateSelected = calendar.timeInMillis
    private lateinit var catSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_purchase)

        model = DbManager(this.applicationContext)
        categories.addAll(model.getListOfCategories())
        categories.forEach{categoryStrings.add(it.categoryName)}
        initSpinner()
        handleDate()
    }

    private fun initSpinner(){
        catSpinner = findViewById(R.id.category_spinner)
        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryStrings)
        catSpinner.adapter = spinnerAdapter
    }

    private fun handleDate(){
        dateSelector = findViewById(R.id.date_selector)
        //dateSelector.minDate = //to avoid crash if user goes beyond 1970 (not wise)
        dateSelector.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calDate = Calendar.getInstance()
            calDate.set(year, month, dayOfMonth)
            dateSelected = calDate.timeInMillis
        }
    }

    fun onAddClicked(view: View){
        val price = findViewById<EditText>(R.id.price_field).text.toString()
        val name = findViewById<EditText>(R.id.name_field).text.toString()
        val cat = findViewById<Spinner>(R.id.category_spinner).selectedItemPosition
        if(price.isBlank() || name.isBlank()) {
            MsgUtils.createDialog("Warning", "Name and Price should not be empty", this).show()
        }
        else if(categoryStrings.isEmpty()) {
            MsgUtils.createDialog("Warning", "You must first add a new category before adding a purchase", this).show()
        }
        else{
            val purchase = Purchase(name, price.toFloat(), dateSelected, "", categories[cat].cid)
            addPurchaseToDb(purchase)
            MsgUtils.showToast("Purchase has been added!", this)
            if(model.isCurrentMonth(dateSelected)){
                sendPriceToMain(price.toFloat())
            }else sendPriceToMain(0f)
        }
    }

    private fun sendPriceToMain(price: Float){
        val myIntent = Intent()
        myIntent.putExtra("price", price)
        setResult(RESULT_OK, myIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Resume", "OnResume Called")
    }

    fun onAddCatClicked(view: View){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Add Category")
        val newCatName = EditText(this)
        newCatName.hint = "Category Name"
        alert.setView(newCatName)
        alert.setPositiveButton("OK"){ _, _  ->
            val name = newCatName.text.toString()
            Log.d("CatName", "new cat: $name")
            when {
                name.isBlank() -> {
                    MsgUtils.showToast("New category can't be empty!", this)
                }
                categoryStrings.contains(name) -> {
                    MsgUtils.showToast("Category already exists!", this)
                    catSpinner.setSelection(spinnerAdapter.getPosition(name)) //set the spinner item to newly added category
                }
                else -> {
                    addCategoryToDb(name)
                    categoryStrings.add(name)
                    spinnerAdapter.notifyDataSetChanged()
                    catSpinner.setSelection(spinnerAdapter.getPosition(name)) //set the spinner item to newly added category
                    MsgUtils.showToast("New Category Added!", this)
                }
            }
        }
        alert.setNegativeButton("Cancel"){ _, _ ->
        }
        alert.show()
    }

    private fun addCategoryToDb(name: String){
        val newCat = Category(name)
        model.addCategory(newCat)
        categories.add(model.getListOfCategories().last())
        Log.d("categories", "Categories list length: ${categories.size}")
    }

    private fun addPurchaseToDb(purchase: Purchase){
        model.addPurchase(purchase)
        Log.d("PurchaseToAdd", "Purchase to add: ${purchase.name} ${purchase.price} ${purchase.date}")
        for(p in model.getAllPurchasesThisMonth()){
            Log.d("Purchases", "Purchase: ${p.name}")
        }
    }
}