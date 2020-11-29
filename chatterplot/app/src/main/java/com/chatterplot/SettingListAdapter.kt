package com.chatterplot

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.setting_item.view.*

class SettingListAdapter(context: Context, items:ArrayList<String>): ArrayAdapter<String>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)!!
        val listItemView = convertView ?:
            LayoutInflater.from(context).inflate(R.layout.setting_item, parent, false)!!
        listItemView.setting_option.text = item
//        return super.getView(position, convertView, parent)
        return listItemView
    }
}