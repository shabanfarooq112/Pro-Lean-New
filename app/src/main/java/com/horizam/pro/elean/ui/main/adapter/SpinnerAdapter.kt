package com.horizam.pro.elean.ui.main.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.horizam.pro.elean.data.model.SpinnerModel


internal class SpinnerAdapter(
    context: Context?,
    textViewResourceId: Int,
    modelArrayList: List<SpinnerModel>,

) :
    ArrayAdapter<SpinnerModel>(context!!, textViewResourceId, modelArrayList) {
    private val myArrayList: List<SpinnerModel> = modelArrayList
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }


    @Nullable
    override fun getItem(position: Int): SpinnerModel {
        return myArrayList[position]
    }

    override fun getCount(): Int {
        return myArrayList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    private fun getCustomView(position: Int, parent: ViewGroup): View {
        val model: SpinnerModel = getItem(position)
        val spinnerRow: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_spinner_dropdown_item, parent, false)
        val label: TextView = spinnerRow.findViewById(R.id.text1)
        label.text = String.format("%s", model.value)
        return spinnerRow
    }

}