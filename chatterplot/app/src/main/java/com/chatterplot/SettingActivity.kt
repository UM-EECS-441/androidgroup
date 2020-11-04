package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class SettingActivity : AppCompatActivity() {
    private lateinit var settingListAdapter: SettingListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        settingListAdapter = SettingListAdapter(this, arrayListOf("Database", "Import"))
        val listView = findViewById<ListView>(R.id.setting_list)
        listView.adapter = settingListAdapter
    }
}