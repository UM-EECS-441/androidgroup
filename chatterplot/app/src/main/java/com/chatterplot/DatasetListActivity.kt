package com.chatterplot

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dataset_list.*
import kotlinx.android.synthetic.main.activity_insert_data.*
import java.io.File

class DatasetListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataset_list)

        val datasetList = getDatabases()

        recycler_view.adapter = DatasetRecyclerViewAdapter(datasetList)
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun getDatabases() : ArrayList<DatasetCard>{
        val datasetNameList = DatabaseHelper(this).getAllDatabaseNames()
        val cardList = ArrayList<DatasetCard>()
        for (i in 0 until datasetNameList.size) {
            val img = File(this.filesDir, "${datasetNameList[i]}.png")
            if(img.isFile) {
                val drawableImg = Drawable.createFromPath(img.absolutePath)
                val newCard = DatasetCard(R.drawable.graph_placeholder, datasetNameList[i], drawableImg)
            } else {
                val newCard = DatasetCard(R.drawable.graph_placeholder, datasetNameList[i])
                cardList += newCard
            }
        }
        return cardList
    }
}