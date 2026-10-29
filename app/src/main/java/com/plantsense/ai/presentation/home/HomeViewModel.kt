package com.plantsense.ai.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.R
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.model.isHealthyDiagnosis
import com.plantsense.ai.domain.repository.ImageStorage
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import com.plantsense.ai.presentation.model.ScanCardUiModel
import com.plantsense.ai.presentation.model.VigorRank
import com.plantsense.ai.presentation.model.toUiModel
import com.plantsense.ai.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

sealed interface HomeEvent {
    data class ImagePicked(val uriString: String) : HomeEvent
    data class DiseaseImagePicked(val uriString: String) : HomeEvent
}

sealed interface HomeEffect {
    data class NavigateToIdentify(val imagePath: String) : HomeEffect
    data class NavigateToDisease(val imagePath: String) : HomeEffect
    data class ShowError(val message: UiText) : HomeEffect
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    @Immutable
    data class Success(
        val scans: ImmutableList<ScanCardUiModel>,
        val totalScans: Int,
        val vigorScore: Int,
        val vigorRank: VigorRank
    ) : HomeUiState
    data object Empty : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    private val imageStorage: ImageStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _isCopying = MutableStateFlow(false)
    val isCopying: StateFlow<Boolean> = _isCopying.asStateFlow()

    val uiState: StateFlow<HomeUiState> = getScanHistoryUseCase()
        .map { list ->
            if (list.isEmpty()) {
                HomeUiState.Empty
            } else {
                val totalScans = list.size

                // Calculate average Vigor Score
                val sum = list.sumOf { item ->
                    if (item.type == ScanType.IDENTIFICATION) {
                        (item.confidence ?: 0.8) * 100
                    } else {
                        val isHealthy = item.isHealthyDiagnosis
                        if (isHealthy) {
                            100.0
                        } else {
                            when (item.diseaseSeverity?.lowercase(Locale.ROOT)) {
                                "low" -> 70.0
                                "medium" -> 45.0
                                "high" -> 15.0
                                else -> 50.0
                            }
                        }
                    }
                }
                val vigorScore = if (totalScans > 0) (sum / totalScans).toInt() else 0

                // Determine Vigor Rank based on scan milestones
                val vigorRank = when {
                    totalScans <= 2 -> VigorRank.NOVICE
                    totalScans <= 5 -> VigorRank.EXPLORER
                    totalScans <= 9 -> VigorRank.SPECIALIST
                    else -> VigorRank.EXPERT
                }

                HomeUiState.Success(
                    scans = list.take(3).map { it.toUiModel() }.toImmutableList(),
                    totalScans = totalScans,
                    vigorScore = vigorScore,
                    vigorRank = vigorRank
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ImagePicked -> processImage(event.uriString, isDisease = false)
            is HomeEvent.DiseaseImagePicked -> processImage(event.uriString, isDisease = true)
        }
    }

    private fun processImage(uriString: String, isDisease: Boolean) {
        viewModelScope.launch {
            _isCopying.value = true
            try {
                val path = withContext(ioDispatcher) {
                    imageStorage.copyUriToStorage(uriString)
                }
                if (path != null) {
                    if (isDisease) {
                        _effects.send(HomeEffect.NavigateToDisease(path))
                    } else {
                        _effects.send(HomeEffect.NavigateToIdentify(path))
                    }
                } else {
                    _effects.send(
                        HomeEffect.ShowError(
                            UiText.StringResource(R.string.failed_to_process_image)
                        )
                    )
                }
            } catch (e: Exception) {
                _effects.send(
                    HomeEffect.ShowError(
                        UiText.StringResource(R.string.failed_to_process_image)
                    )
                )
            } finally {
                _isCopying.value = false
            }
        }
    }

}
