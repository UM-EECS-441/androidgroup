package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class DisplayDataActivity : AppCompatActivity() {
    var loader: TableLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
    }
}