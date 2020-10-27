package com.chatterplot

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dataset_card.view.*

class DatasetRecyclerViewAdapter(private val datasetCardList: List<DatasetCard>) : RecyclerView.Adapter<DatasetRecyclerViewAdapter.DatasetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatasetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.dataset_card,
            parent, false)

        return DatasetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DatasetViewHolder, position: Int) {
        val currentItem = datasetCardList[position]

        holder.imageView.setImageResource(currentItem.imageResource)
        holder.textView.text = currentItem.name
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            val intent = Intent(context, InsertDataActivity::class.java)
            intent.putExtra("DATASETNAME", currentItem.name)
            context.startActivity(intent)
        }

        holder.itemView.deleteDatasetButton.setOnClickListener {
            DatabaseHelper(context).deleteTable(currentItem.name)
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            val t = Toast.makeText(context, "Dataset Deleted", Toast.LENGTH_LONG)
            t.show()
        }
    }

    override fun getItemCount() = datasetCardList.size

    class DatasetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.dataset_graph_preview
        val textView: TextView = itemView.dataset_name
    }
}