package com.stato.jewintage.util

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class NumberEditTextPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs) {

    init {
        setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }
}

