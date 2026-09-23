package com.chommiehub.app.ui.student

import android.view.View
import android.widget.AdapterView

/**
 * Small reusable AdapterView listener used when only the selected-item callback is required.
 */
class SimpleItemSelectedListener(private val onSelected: () -> Unit) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onSelected()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
}