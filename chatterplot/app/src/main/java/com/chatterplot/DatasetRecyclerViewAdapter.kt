package com.chatterplot

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, InsertDataActivity::class.java)
            intent.putExtra("DATASETNAME", currentItem.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = datasetCardList.size

    class DatasetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.dataset_graph_preview
        val textView: TextView = itemView.dataset_name
    }
}