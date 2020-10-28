package com.chatterplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dataset_list.*


class DatabaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataset_list)

        val datasetList = getDatabases()

        recycler_view.adapter = DatasetRecyclerViewAdapterDisplay(datasetList)
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun getDatabases() : List<DatasetCard>{
        val datasetNameList = DatabaseHelper(this).getAllDatabaseNames()
        val cardList = ArrayList<DatasetCard>()
        for (i in 0 until datasetNameList.size) {
            val newCard = DatasetCard(R.drawable.graph_placeholder, datasetNameList[i])
            cardList += newCard
        }
        return cardList
    }
}

//class DatabaseActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d("heredude", "yes")
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_database)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//
//        getListOfTables(findViewById<ListView>(R.id.datasetList))
//    }
//
//    fun getListOfTables(lv: ListView) {
//        val dbList = DatabaseHelper(this).getAllDatabase()
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dbList)
//        lv.adapter = adapter
//        lv.setOnItemClickListener() { _, _, position, _ ->
//            // start intent with dataset name at position
//            var intent = Intent(this, DisplayDataActivity::class.java)
//            intent.putExtra("DATASET", dbList[position])
//            startActivity(intent)
//        }
////        val db = DatabaseHelper(this)
////        db.insertRow("test5", arrayListOf("hello", "world"))
////        val info = db.getTable("test5")
////        text.text = dbList.joinToString("\n")
////        text.text = info.toString()
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            android.R.id.home-> {
//                finish()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//}