package com.stato.jewintage.dialogHelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.accountHelper.AccountHelper
import com.stato.jewintage.databinding.SignDialogBinding

class DialogHelper(act: MainActivity) {
    private val activity = act
    val accountHelper = AccountHelper(activity)

    fun createSignDialog(index:Int) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SignDialogBinding.inflate(activity.layoutInflater)
        builder.setView(bindingDialog.root)

        setDialogState(index, bindingDialog)

        val dialog = builder.create()

        bindingDialog.btnReg.setOnClickListener {
            setOnClickSignUpIn(index, bindingDialog, dialog)
        }
        bindingDialog.btnForgotPsw.setOnClickListener {
            setOnClickResetPassword(bindingDialog, dialog)
        }
        bindingDialog.btnsignInWithGoogle.setOnClickListener {
            accountHelper.signInWithGoogle()
            dialog.dismiss()
        }
            dialog.show()

    }

    private fun setOnClickResetPassword(bindingDialog: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if (bindingDialog.edEmailInput.text?.isNotEmpty()!!){
            activity.auth.sendPasswordResetEmail(bindingDialog.edEmailInput.text.toString()).addOnCompleteListener { task->
                if (task.isSuccessful){
                    Toast.makeText(activity,
                        R.string.reg_reset_password,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setOnClickSignUpIn(index: Int, bindingDialog: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if (index == DialogConst.SIGN_UP_STATE) {
            accountHelper.signUpWithEmail(
                bindingDialog.edEmailInput.text.toString(),
                bindingDialog.edPasswordInput.text.toString()
            )
        } else {
            accountHelper.signInWithEmail(
                bindingDialog.edEmailInput.text.toString(),
                bindingDialog.edPasswordInput.text.toString()
            )

        }
    }

    private fun setDialogState(index: Int, bindingDialog: SignDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            bindingDialog.regTitle.text = activity.resources.getString(R.string.reg_title)
            bindingDialog.btnReg.text = activity.resources.getString(R.string.reg_btnreg)
        } else {
            bindingDialog.btnForgotPsw.visibility = View.VISIBLE
            bindingDialog.regTitle.text = activity.resources.getString(R.string.reg_login)
            bindingDialog.btnReg.text = activity.resources.getString(R.string.reg_loginBtn)
        }
    }

}