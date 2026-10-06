package com.shokirjon.sonettube.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokirjon.sonettube.model.Video
import com.shokirjon.sonettube.repository.FavoriteRepository
import com.shokirjon.sonettube.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val video: Video? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
)

class PlayerViewModel(
    private val videoId: String,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val video = historyRepository.findById(videoId)
                ?: favoriteRepository.findById(videoId)
            _uiState.update {
                it.copy(video = video, isFavorite = favoriteRepository.isFavorite(videoId), isLoading = false)
            }
        }
    }

    fun toggleFavorite() {
        val video = _uiState.value.video ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) favoriteRepository.remove(video.videoId)
            else favoriteRepository.save(video)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }
}
