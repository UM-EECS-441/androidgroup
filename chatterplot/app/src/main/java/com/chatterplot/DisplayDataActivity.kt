package com.chatterplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomappbar.BottomAppBar


class DisplayDataActivity : AppCompatActivity() {
    lateinit var adapter: TableAdapter
    private val INSERT_REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tableName = intent.getStringExtra("DATASETNAME")
        //create tableAdapter
        adapter = TableAdapter(this, tableName)
        adapter.loadTable()
        //intent should include DB path/name and specified DB to open

        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.graph_dataset -> {
                    val intent = Intent(this, GraphActivity::class.java)
                    intent.putExtra("DATASETNAME", tableName)
                    startActivity(intent)
                    true
                }
                R.id.create_dataset -> {
                    val intent = Intent(this, InsertDataActivity::class.java)
                    intent.putExtra("DATASETNAME", tableName)
                    startActivityForResult(intent, INSERT_REQUEST_CODE)
                    true
                }
                R.id.setting_dataset -> {
                    Toast.makeText(this, "Setting not implemented yet", Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home-> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INSERT_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.refreshTable()
        }
    }
}