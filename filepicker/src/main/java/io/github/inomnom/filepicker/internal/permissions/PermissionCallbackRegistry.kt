package io.github.inomnom.filepicker.internal.permissions

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object PermissionCallbackRegistry {
    private val handlers = ConcurrentHashMap<String, PermissionHandler>()

    fun register(handler: PermissionHandler): String {
        val id = UUID.randomUUID().toString()
        handlers[id] = handler
        return id
    }

    fun unregister(id: String) {
        handlers.remove(id)
    }

    fun notifyGranted(id: String) {
        val handler = handlers.remove(id)
        handler?.onGranted()
    }

    fun notifyDenied(id: String) {
        val handler = handlers.remove(id)
        handler?.onDenied()
    }

    fun clear() {
        handlers.clear()
    }
}