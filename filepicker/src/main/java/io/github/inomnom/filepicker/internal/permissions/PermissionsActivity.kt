package io.github.inomnom.filepicker.internal.permissions

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.inomnom.filepicker.R
import io.github.inomnom.filepicker.BuildConfig

internal class PermissionsActivity : AppCompatActivity() {

    private var allPermissionsRequested: List<String> = emptyList()
    private var requiresSettingsDialog = false
    private var showSettingsDialog = false
    private var callbackId = ""

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            handlePermissionsResult(permissionsResult)
        }

    private val settingsActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkPermissionsAndProceed()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            finish()
            return
        }

        allPermissionsRequested = intent.getStringArrayListExtra(EXTRA_PERMISSIONS) ?: emptyList()
        showSettingsDialog = intent.getBooleanExtra(EXTRA_SHOW_SETTINGS, false)
        callbackId = intent.getStringExtra(EXTRA_CALLBACK_ID) ?: ""

        if (allPermissionsRequested.isEmpty()) {
            grant()
            return
        }

        checkPermissionsAndProceed()
    }

    private fun checkPermissionsAndProceed() {
        val permissionsToRequest = allPermissionsRequested.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            grant()
        } else {
            if (requiresSettingsDialog) {
                deny()
            } else {
                requestPermissionsLauncher.launch(permissionsToRequest)
            }
        }
    }

    private fun handlePermissionsResult(permissionsResult: Map<String, Boolean>) {
        val allGranted = permissionsResult.all { it.value }

        if (allGranted) {
            grant()
        } else {
            val permanentlyDenied = allPermissionsRequested.any { permission ->
                !ActivityCompat.shouldShowRequestPermissionRationale(this, permission) &&
                        ContextCompat.checkSelfPermission(
                            this,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
            }

            if (permanentlyDenied && showSettingsDialog) {
                showSettingsDialog()
            } else {
                deny()
            }
        }
    }

    private fun showSettingsDialog() {
        requiresSettingsDialog = true
        val deniedPermissionsList = allPermissionsRequested.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        val permissionNames = deniedPermissionsList.joinToString(", ") { permission ->
            runCatching {
                packageManager.getPermissionInfo(permission, 0).loadLabel(packageManager).toString()
            }.getOrDefault(permission.substringAfterLast('.'))
        }

        val message = getString(R.string.fp_permission_settings_message, permissionNames)
        val settingsButton = getString(R.string.fp_permission_settings_button)
        val cancelButton = getString(R.string.fp_cancel)

        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(settingsButton) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                settingsActivityResultLauncher.launch(intent)
            }
            .setNegativeButton(cancelButton) { _, _ ->
                deny()
            }
            .show()
    }

    private fun grant() {
        PermissionCallbackRegistry.notifyGranted(callbackId)
        finish()
    }

    private fun deny() {
        PermissionCallbackRegistry.notifyDenied(callbackId)
        finish()
    }

    companion object {
        internal const val EXTRA_PERMISSIONS = "${BuildConfig.LIBRARY_PACKAGE_NAME}.PERMISSIONS"
        internal const val EXTRA_SHOW_SETTINGS = "${BuildConfig.LIBRARY_PACKAGE_NAME}.SHOW_SETTINGS"
        internal const val EXTRA_CALLBACK_ID = "${BuildConfig.LIBRARY_PACKAGE_NAME}.CALLBACK_ID"
    }
}