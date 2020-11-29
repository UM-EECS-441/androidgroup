package com.chatterplot

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dataset_card.view.*


open class DatasetRecyclerViewAdapter(private var datasetCardList: ArrayList<DatasetCard>) : RecyclerView.Adapter<DatasetRecyclerViewAdapter.DatasetViewHolder>() {
    private var datasetListCopy = ArrayList(datasetCardList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatasetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.dataset_card,
            parent, false)

        return DatasetViewHolder(itemView)
    }

    fun addItem(name: String) {
        val card = DatasetCard(R.drawable.graph_placeholder, name)
        datasetCardList.add(0, card)
        datasetListCopy.add(0, card)
        this.notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DatasetViewHolder, position: Int) {
        val currentItem = datasetCardList[position]
        if(currentItem.preview == null) {
            holder.imageView.setImageResource(currentItem.imageResource)
        }
        else {
            holder.imageView.setImageDrawable(currentItem.preview)
        }

        holder.textView.text = currentItem.name
        val context = holder.itemView.context

        holder.itemView.editButton.setOnClickListener {
            val intent = Intent(context, InsertDataActivity::class.java)
            intent.putExtra("DATASETNAME", currentItem.name)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DisplayDataActivity::class.java)
            intent.putExtra("DATASETNAME", currentItem.name)
            context.startActivity(intent)
        }

        holder.itemView.deleteDatasetButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val deleteMessage = "Are you sure you want to delete " + currentItem.name + "?"
            builder.setMessage(deleteMessage)
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        DatabaseHelper(context).deleteTable(currentItem.name)
                        val toastString = "Dataset \"" + currentItem.name + "\" Deleted"
                        val t = Toast.makeText(context, toastString, Toast.LENGTH_LONG)
                        t.show()

                        datasetCardList.remove(currentItem)
                        datasetListCopy.remove(currentItem)
                        this.notifyDataSetChanged()
//                        val intent = Intent(context, MainActivity::class.java)
//                        context.startActivity(intent)
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun getItemCount() = datasetCardList.size

    fun filter(query: String?) {
        if(query.isNullOrEmpty()) {
            datasetCardList = ArrayList(datasetListCopy)
        } else {
            datasetCardList.clear()
            for(item in datasetListCopy) {
                if(item.name.toLowerCase().contains(query.toLowerCase())) {
                    datasetCardList.add(item)
                }
            }
        }
        this.notifyDataSetChanged()
    }

    class DatasetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.dataset_graph_preview
        val textView: TextView = itemView.dataset_name
    }
}