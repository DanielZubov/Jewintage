package com.stato.jewintage.accountHelper

import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.stato.jewintage.MainActivity
import com.stato.jewintage.R
import com.stato.jewintage.constance.FirebaseConstance
import com.stato.jewintage.dialogHelper.GoogleAccConst

class AccountHelper(act: MainActivity) {
    private val activity = act
    private lateinit var signInClient: GoogleSignInClient


    fun signUpWithEmail(email:String, password:String){
        if (email.isNotEmpty() && password.isNotEmpty()){
            activity.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    sendEmailVerification(task.result.user!!)
                    activity.uiUpdate(task.result.user)
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException){
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        if (exception.errorCode == FirebaseConstance.ERROR_EMAIL_ALREADY_IN_USE){
                            Toast.makeText(activity, FirebaseConstance.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_SHORT).show()
                            //Link email
                            linkEmailToGoogle(email, password)
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseConstance.ERROR_INVALID_EMAIL){
                            Toast.makeText(activity, FirebaseConstance.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException){
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseConstance.ERROR_WEAK_PASSWORD){
                            Toast.makeText(activity, FirebaseConstance.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun signInWithEmail(email:String, password:String){
        if (email.isNotEmpty() && password.isNotEmpty()){
            activity.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    activity.uiUpdate(task.result.user)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseConstance.ERROR_INVALID_EMAIL){
                            Toast.makeText(activity, FirebaseConstance.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseConstance.ERROR_WRONG_PASSWORD){
                            Toast.makeText(activity, FirebaseConstance.ERROR_WRONG_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                }


            }
        }
    }

    private fun linkEmailToGoogle(email: String, password: String){
        val credential = EmailAuthProvider.getCredential(email, password)
        if (activity.auth.currentUser != null){
        activity.auth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener {task ->
        if (task.isSuccessful) {
            Toast.makeText(activity, activity.resources.getString(R.string.link_done), Toast.LENGTH_SHORT)
                .show()
        }
        }
    }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)

    }

    fun signInWithGoogle(){
        signInClient = getSignInClient()
        val i = signInClient.signInIntent
        activity.googleSignInLauncher.launch(i)
    }
    fun signOutGoogle(){
        getSignInClient().signOut()
    }
    fun signInFirebaseWithGoogle(token:String){
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful){
                activity.uiUpdate(task.result.user)
                Toast.makeText(activity, "Вход выполнен успешно", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendEmailVerification(user:FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener { task->
            if (task.isSuccessful){
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_error),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


}