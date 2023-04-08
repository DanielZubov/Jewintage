package com.stato.jewintage.dialogHelper

import android.app.Activity
import android.app.AlertDialog
import com.stato.jewintage.databinding.ProgressDialogLayoutBinding

object ProgressDialog {

    fun createProgressDialog(activity: Activity) : AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = ProgressDialogLayoutBinding.inflate(activity.layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog

    }

}