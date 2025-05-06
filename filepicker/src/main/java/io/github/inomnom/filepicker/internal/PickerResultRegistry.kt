// File: /mnt/data/filepicker_extracted/internal/PickerResultRegistry.kt
package io.github.inomnom.filepicker.internal

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.inomnom.filepicker.api.PickerResult
import java.util.concurrent.ConcurrentHashMap

internal object PickerResultRegistry {
    private val TAG = PickerResultRegistry::class.java.simpleName
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, (PickerResult) -> Unit>()

    fun register(id: String, callback: (PickerResult) -> Unit) {
        if (callbacks.containsKey(id)) {
            Log.w(TAG, "Duplicate request ID registration attempted: $id. Overwriting.")
        }
        callbacks[id] = callback
    }

    fun deliverResult(id: String, result: PickerResult) {
        val callback = callbacks.remove(id)
        if (callback != null) {
            mainThreadHandler.post {
                callback(result)
            }
        } else {
            Log.w(TAG, "No callback found to deliver result for ID: $id. Result: $result")
        }
    }

    fun unregister(id: String) {
        callbacks.remove(id)
    }

    fun clear() {
        callbacks.clear()
    }
}