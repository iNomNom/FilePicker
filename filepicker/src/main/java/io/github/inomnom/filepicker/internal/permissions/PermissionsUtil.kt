package io.github.inomnom.filepicker.internal.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

internal object PermissionsUtil {
    fun check(
        context: Context,
        permissions: Array<String>,
        handler: PermissionHandler,
        showSettingsDialog: Boolean,
    ) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            handler.onGranted()
        } else {
            val callbackId = PermissionCallbackRegistry.register(handler)

            val intent = Intent(context, PermissionsActivity::class.java).apply {
                putStringArrayListExtra(
                    PermissionsActivity.EXTRA_PERMISSIONS,
                    ArrayList(permissions.toList())
                )
                putExtra(PermissionsActivity.EXTRA_SHOW_SETTINGS, showSettingsDialog)
                putExtra(PermissionsActivity.EXTRA_CALLBACK_ID, callbackId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}