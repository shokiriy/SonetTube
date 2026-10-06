package com.shokirjon.sonettube.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokirjon.sonettube.model.Video
import com.shokirjon.sonettube.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    val history: StateFlow<List<Video>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun remove(videoId: String) {
        viewModelScope.launch { repository.remove(videoId) }
    }

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }
}
