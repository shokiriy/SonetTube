package com.shokirjon.sonettube.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokirjon.sonettube.repository.ApiErrorReason
import com.shokirjon.sonettube.repository.ApiKeyMissingException
import com.shokirjon.sonettube.repository.YouTubeApiException
import com.shokirjon.sonettube.repository.YouTubeRepository
import com.shokirjon.sonettube.model.Video
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val videos: List<Video> = emptyList(),
    val hasSearched: Boolean = false,
    val error: HomeError? = null,
)

enum class HomeError {
    EMPTY_QUERY,
    API_KEY_MISSING,
    INVALID_KEY_OR_QUOTA,
    NETWORK,
    SERVER,
}

class HomeViewModel(private val repository: YouTubeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(error = HomeError.EMPTY_QUERY, hasSearched = true) }
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasSearched = true, error = null) }
            try {
                val videos = repository.search(query)
                _uiState.update { it.copy(isLoading = false, videos = videos) }
            } catch (_: ApiKeyMissingException) {
                _uiState.update { it.copy(isLoading = false, error = HomeError.API_KEY_MISSING) }
            } catch (error: YouTubeApiException) {
                val mappedError = when (error.reason) {
                    ApiErrorReason.INVALID_KEY_OR_QUOTA -> HomeError.INVALID_KEY_OR_QUOTA
                    ApiErrorReason.NETWORK -> HomeError.NETWORK
                    ApiErrorReason.SERVER -> HomeError.SERVER
                }
                _uiState.update { it.copy(isLoading = false, error = mappedError) }
            }
        }
    }
}
