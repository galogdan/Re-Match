package com.example.re_match.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


// handle permission
object PermissionUtils {
    const val CAMERA_PERMISSION_CODE = 101
    const val GALLERY_PERMISSION_CODE = 102

    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(fragment: Fragment, permission: String, requestCode: Int) {
        when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    fragment.requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), requestCode)
                } else {
                    fragment.requestPermissions(arrayOf(permission), requestCode)
                }
            }
            else -> fragment.requestPermissions(arrayOf(permission), requestCode)
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            onDenied()
        }
    }

    fun getRequiredPermissionForGallery(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}