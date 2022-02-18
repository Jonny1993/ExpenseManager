package com.example.expensemanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import model.DbManager
import uiutils.MsgUtils
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var model: DbManager
    private var budget = 60000
    private val ADD_REQUEST_CODE = 1000
    private val VIEW_REQUEST_CODE = 1001
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var budgetSpentPercent: TextView
    private var budgetSpent = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        model = DbManager(this.applicationContext)
        readBudgetFromFile()
        updateCurrentBudget()
        updateDaysGone()
        getBudgetSpent()
        updateBudgetSpent()
        showBarChart()
    }

    private fun initViews(){
        circularProgressBar = findViewById(R.id.circularProgressBar)
        budgetSpentPercent = findViewById(R.id.progress_text)
    }

    private fun readBudgetFromFile(){
        if (this.fileList().contains("budget")) {
            openFileInput("budget").bufferedReader().use {
                budget = it.readLine().toInt()
            }
        }else firstRun()
    }

    private fun firstRun(){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("First Run")
        alert.setMessage("Please type in your budget")
        val newBudget = EditText(this)
        newBudget.inputType = InputType.TYPE_CLASS_NUMBER
        newBudget.hint = "New Budget"
        alert.setView(newBudget)
        alert.setPositiveButton("OK"){_, _ ->
            val result = newBudget.text.toString()
            if(result.isNotEmpty()) {
                budget = result.toInt()
            }else MsgUtils.showToast("No value was provided. Keeping default value of $budget", this)
            updateCurrentBudget()
        }
        alert.setCancelable(false)
        alert.show()
    }

    fun onChangeBudgetClick(view: View){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Change Budget")
        val newBudget = EditText(this)
        newBudget.inputType = InputType.TYPE_CLASS_NUMBER
        newBudget.hint = "New Budget"
        alert.setView(newBudget)
        alert.setPositiveButton("OK"){_, _ ->
            val result = newBudget.text.toString()
            if(result.isNotEmpty()) {
                budget = result.toInt()
                updateBudgetSpent()
                updateCurrentBudget()
            }else MsgUtils.showToast("No new value was provided", this)
        }
        alert.setNegativeButton("Cancel"){_, _ -> }
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null) {
            if (requestCode == ADD_REQUEST_CODE) {
                budgetSpent += data.getFloatExtra("price", 0f)
            }
            if (requestCode == VIEW_REQUEST_CODE) {
                Log.d("ViewFin", "View is finished")
                budgetSpent -= data.getFloatExtra("price", 0f)
            }
            updateBudgetSpent()
            showBarChart()
        }
    }

    private fun updateCurrentBudget(){
        findViewById<TextView>(R.id.current_budget).text = getString(R.string.current_budget, budget)
    }

    private fun updateDaysGone(){
        val daysGone = findViewById<ProgressBar>(R.id.progress_month)
        val lastDayOfMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        daysGone.progress = (currentDayOfMonth*100)/lastDayOfMonth
    }

    private fun getBudgetSpent() {
        budgetSpent = model.getSumForThisMonth()
    }

    private fun updateBudgetSpent(){
        val percentageSpent = (budgetSpent/budget)*100
        budgetSpentPercent.text = getString(R.string.percent, percentageSpent.toInt())
        if(budgetSpent > budget) circularProgressBar.progressBarColor = Color.RED
        else circularProgressBar.progressBarColor = Color.parseColor("#3f51b5")
        circularProgressBar.progress = percentageSpent
        circularProgressBar.progress = percentageSpent //called twice because sometimes it doesn't update
    }

    fun onAddPurchaseClicked(view: View){
        val myIntent = Intent(this, AddPurchaseActivity::class.java)
        startActivityForResult(myIntent, ADD_REQUEST_CODE)
    }

    fun onProgressClicked(view: View){
        val myIntent = Intent(this, ViewPurchasesActivity::class.java)
        startActivityForResult(myIntent, VIEW_REQUEST_CODE)
    }

    private fun showBarChart(){
        val chart = findViewById<BarChart>(R.id.bar_chart)
        val entries = ArrayList<BarEntry>()
        val data = model.getHighestSpentOnCategoriesThisMonth()
        var i = 0f
        val labels = ArrayList<String>()
        for(cat in data){
            labels.add(cat.categoryName)
            val spent = model.getSumByTagForThisMonth(cat.cid)
            entries.add(BarEntry(i, spent))
            i++
        }
        val dataSet = BarDataSet(entries, "Category")
        val barData = BarData(dataSet)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.isGranularityEnabled = true
        val descr = Description()
        descr.text = ""
        chart.description = descr
        chart.data = (barData)
        chart.invalidate()
    }

    override fun onPause() {
        super.onPause()
        saveBudgetToFile()
    }

    private fun saveBudgetToFile(){
        openFileOutput("budget", Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(budget.toString())
        }
    }
}