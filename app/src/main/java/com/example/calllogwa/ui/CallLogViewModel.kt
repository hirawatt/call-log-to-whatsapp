package com.example.calllogwa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calllogwa.data.CallEntry
import com.example.calllogwa.data.CallLogRepository
import com.example.calllogwa.data.WaNameFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallLogViewModel(private val repository: CallLogRepository) : ViewModel() {

    private val _rawCallLogs = MutableStateFlow<List<CallEntry>>(emptyList())
    
    private val _filteredCallLogs = MutableStateFlow<List<CallEntry>>(emptyList())
    val callLogs: StateFlow<List<CallEntry>> = _filteredCallLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentSearchQuery: String = ""

    fun loadCallLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            val logs = repository.getCallLogs()
            _rawCallLogs.value = logs
            applyFilter(currentSearchQuery)
            _isLoading.value = false

            // Asynchronously fetch WA Business profile names in background for valid WA numbers
            fetchWaBusinessNames(logs)
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query.trim()
        applyFilter(currentSearchQuery)
    }

    private fun applyFilter(query: String) {
        if (query.isBlank()) {
            _filteredCallLogs.value = _rawCallLogs.value
        } else {
            val lowercaseQuery = query.lowercase()
            _filteredCallLogs.value = _rawCallLogs.value.filter { entry ->
                entry.displayTitle.lowercase().contains(lowercaseQuery) ||
                        entry.number.contains(lowercaseQuery) ||
                        entry.formattedNumber.contains(lowercaseQuery)
            }
        }
    }

    private fun fetchWaBusinessNames(logs: List<CallEntry>) {
        viewModelScope.launch {
            val updatedLogs = _rawCallLogs.value.toMutableList()
            var modified = false

            for (i in updatedLogs.indices) {
                val entry = updatedLogs[i]
                if (entry.hasWhatsApp && entry.waBusinessName == null) {
                    val bizName = WaNameFetcher.fetchBusinessName(entry.number)
                    if (!bizName.isNullOrBlank()) {
                        updatedLogs[i] = entry.copy(waBusinessName = bizName)
                        modified = true
                    }
                }
            }

            if (modified) {
                _rawCallLogs.value = updatedLogs
                applyFilter(currentSearchQuery)
            }
        }
    }
}

class CallLogViewModelFactory(private val repository: CallLogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
