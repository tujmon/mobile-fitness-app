package com.hackerfit

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PendingImportState {
    private val _pendingUri = MutableStateFlow<Uri?>(null)
    val pendingUri: StateFlow<Uri?> = _pendingUri.asStateFlow()

    fun setPendingImport(uri: Uri) {
        _pendingUri.value = uri
    }

    fun clear() {
        _pendingUri.value = null
    }
}
