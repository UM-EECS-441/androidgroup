package com.chatterplot

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TableLayout



class TableLoader(context: Context, val tableNameDB: String) {
    var tableToPopulate: ViewGroup = (context as DisplayDataActivity).findViewById<TableLayout>(R.id.dataDisplayTable)
    var columns: Int? = null

    private fun getData() {
        // TODO
    }

    private fun addTableRow(index: Int) {
        // TODO
    }
}