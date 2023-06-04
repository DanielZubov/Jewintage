package com.stato.jewintage.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsManager(private val activity: Activity) {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 123
    }

    fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            // Разрешение на камеру уже предоставлено
            return true
        } else {
            // Разрешения еще не предоставлены, запрашиваем их
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                PERMISSIONS_REQUEST_CODE
            )
            return false
        }
    }

}
