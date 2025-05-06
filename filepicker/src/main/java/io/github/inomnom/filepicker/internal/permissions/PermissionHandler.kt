package io.github.inomnom.filepicker.internal.permissions

interface PermissionHandler {
    fun onGranted()
    fun onDenied()
}