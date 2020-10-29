package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem


class DisplayDataActivity : AppCompatActivity() {
    lateinit var adapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tableName = intent.getStringExtra("DATASETNAME")
        //create tableAdapter
        adapter = TableAdapter(this, tableName)
        adapter.loadTable()
        //intent should include DB path/name and specified DB to open
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home-> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}