package com.example.expensemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.widget.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import model.DbManager
import model.ExpenseManagerExecutor
import model.Purchase
import uiutils.MsgUtils
import uiutils.PurchasesAdapter
import java.util.*
import kotlin.math.abs


class ViewPurchasesActivity : AppCompatActivity() {
    private val purchases = ArrayList<Purchase>()
    private lateinit var model: DbManager
    private lateinit var monthString: String
    private val calendar = Calendar.getInstance()
    private var chosenMonth = calendar.get(Calendar.MONTH)
    private var monthOffset = 0
    private var year = calendar.get(Calendar.YEAR)
    private lateinit var rvItems: RecyclerView
    private lateinit var monthTextView: TextView
    private lateinit var monthTotal: TextView
    private lateinit var sortSpinner: Spinner
    private lateinit var rvAdapter: PurchasesAdapter
    private var priceRemoved = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_purchases)

        initViews()
        model = DbManager(this.applicationContext)
        purchases.addAll(model.getAllPurchasesThisMonth())
        for(p in purchases)
            Log.d("PurchaseItems", "Purchase : ${p.name}")
        initHeaderAndFooter()
        initSpinner()
        populateList()
        handleSpinnerChoice()
    }

    private fun initViews(){
        rvItems = findViewById(R.id.purchases_list)
        monthTextView = findViewById(R.id.month_text)
        monthTotal = findViewById(R.id.monthly_total)
        sortSpinner = findViewById(R.id.sort_spinner)
    }

    private fun initHeaderAndFooter(){
        monthString = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
        Log.d("Month", "Current month: $monthString")
        monthTextView.text = getString(R.string.month_text, monthString, year)
        monthTotal.text = getString(R.string.monthly_total, model.getSumForThisMonth().toString())
    }

    fun getPreviousPurchases(view: View){
        chosenMonth = if(--chosenMonth < 0) chosenMonth+12 else chosenMonth
        Log.d("ChosenMonth", "Chosen Month is $chosenMonth")
        purchases.clear()
        if(chosenMonth==11) year--
        monthString = getMonthFromCalender(chosenMonth)
        Log.d("Month", "Current month: $monthString")
        monthTextView.text = getString(R.string.month_text, monthString, year)
        var total: Float
        ExpenseManagerExecutor.getInstance().diskIo.execute {
            purchases.addAll(model.getAllPurchasesOneMonth(--monthOffset))
            total = model.getSumForOneMonth(monthOffset)
            runOnUiThread {
                populateList()
                monthTotal.text = getString(R.string.monthly_total, total.toString())
            }
        }
        val nextBtn = findViewById<ImageButton>(R.id.next_btn)
        /**
         * In case planned purchases are disallowed
         */
        /*if(!nextBtn.isVisible) {
            nextBtn.visibility = VISIBLE
            nextBtn.isClickable = true
        }*/
        /**
         * Easter egg
         */
        if(year==1970){
            MsgUtils.createDialog("Oops!", "Congratulations!. You have have traced back " +
                    "${abs(monthOffset)} months and you are stuck here forever ;)", this).show()
            val prevBtn = findViewById<ImageButton>(R.id.previous_btn)
            prevBtn.visibility = INVISIBLE
            prevBtn.isClickable = false
            nextBtn.visibility = INVISIBLE
            nextBtn.isClickable = false
        }
    }

    fun getNextPurchases(view: View){
        chosenMonth = (chosenMonth+1).rem(12)
        purchases.clear()
        if(chosenMonth==0)  year++
        monthString =  getMonthFromCalender(chosenMonth)
        Log.d("Month", "Current month: $monthString ChosenMonth: $chosenMonth")
        monthTextView.text = getString(R.string.month_text, monthString, year)
        var total: Float
        ExpenseManagerExecutor.getInstance().diskIo.execute {
            purchases.addAll(model.getAllPurchasesOneMonth(++monthOffset))
            total = model.getSumForOneMonth(monthOffset)
            runOnUiThread {
                populateList()
                monthTotal.text = getString(R.string.monthly_total, total.toString())
            }
        }
        val nextBtn = findViewById<ImageButton>(R.id.next_btn)
        /**
         *  In case future (planned) purchases are not allowed
         */
        /*if(chosenMonth==calendar.get(Calendar.MONTH) && year==calendar.get(Calendar.YEAR)){
            nextBtn.visibility = INVISIBLE
            nextBtn.isClickable = false
        }*/
        /**
         * Easter egg
         */
        if(year==2100){
            MsgUtils.createDialog("Oops!", "Congratulations time traveler! You have successfully " +
                    "time travelled and are stuck here forever D:", this).show()
            val prevBtn = findViewById<ImageButton>(R.id.previous_btn)
            prevBtn.visibility = INVISIBLE
            prevBtn.isClickable = false
            nextBtn.visibility = INVISIBLE
            nextBtn.isClickable = false
        }
    }

    private fun populateList(){
        rvAdapter = PurchasesAdapter(purchases, model)
        rvItems.adapter = rvAdapter
        Log.d("Purchases", "Number of purchases in adapter list: ${rvAdapter.itemCount}")
        rvItems.layoutManager = LinearLayoutManager(this)
    }

    private fun getMonthFromCalender(month: Int): String{
        val months = arrayOf("January", "February", "March",
                "April", "May", "June", "July", "August", "September", "October",
                "November", "December")
        return months[month]
    }

    private fun initSpinner(){
        val sortStrings = arrayOf("Sort by Name", "Sort by Price", "Sort by Cat", "Sort by Date")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sortStrings)
        sortSpinner.adapter = adapter
        sortSpinner.setSelection(adapter.getPosition(sortStrings[0]))
    }

    private fun handleSpinnerChoice(){
        sortSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                when(position){
                    0 -> rvAdapter.sortByName()
                    1 -> rvAdapter.sortByPrice()
                    2 -> rvAdapter.sortByCat()
                    3 -> rvAdapter.sortByDate()
                }
                rvAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }

    fun onDeletePurchClick(view: View){
        val alert = MsgUtils.createCustomDialog("Warning"
                , "Are you sure you want to delete this purchase?", this)
        alert.setNegativeButton("Cancel"){_, _ ->
        }
        alert.setPositiveButton("OK"){_, _ ->
            Log.d("BtnId", "Button ID: ${view.id}")
            if(model.isCurrentMonth(purchases[view.id-1].date)) addPriceToIntent(purchases[view.id-1].price?:0f)
            deletePurchFromDb(purchases[view.id-1])
            purchases.removeAt(view.id-1)
            populateList()
            monthTotal.text = getString(R.string.monthly_total, model.getSumForThisMonth().toString())
        }
        alert.show()
    }

    private fun deletePurchFromDb(purchase: Purchase){
        model.deletePurchase(purchase)
    }

    private fun addPriceToIntent(price: Float){
        priceRemoved+=price
        val myIntent = Intent()
        myIntent.putExtra("price", priceRemoved)
        setResult(RESULT_OK, myIntent)
    }
}