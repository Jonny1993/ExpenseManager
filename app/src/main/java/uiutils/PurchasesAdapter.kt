package uiutils

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import model.DbManager
import model.Purchase
import java.text.SimpleDateFormat
import java.util.*

class PurchasesAdapter(private var mItems: List<Purchase>, private val model: DbManager): RecyclerView.Adapter<PurchasesAdapter.ViewHolder>() {
    inner class ViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView){
        val itemTextView: TextView = itemView.findViewById(R.id.purchases_item_name2)
        val priceTextView: TextView = itemView.findViewById(R.id.purchases_item_price2)
        val catTextView: TextView = itemView.findViewById(R.id.purchases_item_cat2)
        val dateTextView: TextView = itemView.findViewById(R.id.purchases_item_date2)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.delete_btn)
        val resources = itemView.resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchasesAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemsView = inflater.inflate(R.layout.items_purchase, parent, false)
        return ViewHolder(itemsView)
    }

    override fun onBindViewHolder(viewHolder: PurchasesAdapter.ViewHolder, position: Int) {
        viewHolder.deleteBtn.id = position+1 //first item doesn't show up in rv from adapter list if set to position, why??
        val purchaseItem = mItems[position]
        val itemTextView = viewHolder.itemTextView
        itemTextView.text = purchaseItem.name
        val priceTextView = viewHolder.priceTextView
        val res = viewHolder.resources
        priceTextView.text = res.getString(R.string.purchases_item_price, purchaseItem.price.toString())
        val catTextView = viewHolder.catTextView
        val cat = model.findCategory(purchaseItem.categoryID)
        catTextView.text = cat.categoryName
        val dateTextView = viewHolder.dateTextView
        dateTextView.text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(purchaseItem.date)
        Log.d("DeleteBtn", "Del btn ID: ${viewHolder.deleteBtn.id}")
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun sortByName(){
        mItems = mItems.sortedBy { it.name }
    }

    fun sortByPrice(){
        mItems = mItems.sortedBy { it.price }
    }

    fun sortByCat(){
        mItems = mItems.sortedBy { model.findCategory(it.categoryID).categoryName }
    }

    fun sortByDate(){
        mItems = mItems.sortedBy { it.date }
    }
}