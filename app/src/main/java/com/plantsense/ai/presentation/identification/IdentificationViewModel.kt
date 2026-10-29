package com.plantsense.ai.presentation.identification

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.core.error.ErrorMapper
import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.usecase.GetScanHistoryItemUseCase
import com.plantsense.ai.domain.usecase.IdentifyPlantUseCase
import com.plantsense.ai.domain.usecase.SaveScanUseCase
import com.plantsense.ai.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface IdentificationUiState {
    data object Loading : IdentificationUiState
    @Immutable
    data class Success(val result: PlantIdentificationResult) : IdentificationUiState
    @Immutable
    data class Error(val message: UiText) : IdentificationUiState
}

@HiltViewModel
class IdentificationViewModel @Inject constructor(
    private val identifyPlantUseCase: IdentifyPlantUseCase,
    private val saveScanUseCase: SaveScanUseCase,
    private val getScanHistoryItemUseCase: GetScanHistoryItemUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<IdentificationUiState>(IdentificationUiState.Loading)
    val uiState: StateFlow<IdentificationUiState> = _uiState

    private var hasSaved = false

    fun loadResult(historyId: Int, imagePath: String) {
        if (historyId != -1) {
            viewModelScope.launch(ioDispatcher) {
                _uiState.value = IdentificationUiState.Loading
                val item = getScanHistoryItemUseCase(historyId)
                if (item != null) {
                    _uiState.value = IdentificationUiState.Success(
                        PlantIdentificationResult(
                            plantName = item.plantName.orEmpty(),
                            botanicalName = item.botanicalName.orEmpty(),
                            confidence = item.confidence ?: 0.0,
                            description = item.description.orEmpty(),
                            careLight = item.careLight.orEmpty(),
                            careWater = item.careWater.orEmpty(),
                            careTemp = item.careTemp.orEmpty()
                        )
                    )
                } else {
                    _uiState.value = IdentificationUiState.Error(
                        UiText.StringResource(com.plantsense.ai.R.string.scan_unavailable)
                    )
                }
            }
        } else {
            identify(imagePath)
        }
    }

    fun identify(imagePath: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = IdentificationUiState.Loading
            when (val result = identifyPlantUseCase(imagePath)) {
                is NetworkResult.Success -> {
                    _uiState.value = IdentificationUiState.Success(result.data)
                    if (!hasSaved) {
                        hasSaved = true
                        saveScanUseCase(
                            ScanHistoryItem(
                                id = 0,
                                type = ScanType.IDENTIFICATION,
                                imageUrl = imagePath,
                                timestamp = System.currentTimeMillis(),
                                plantName = result.data.plantName,
                                botanicalName = result.data.botanicalName,
                                confidence = result.data.confidence,
                                description = result.data.description,
                                careLight = result.data.careLight,
                                careWater = result.data.careWater,
                                careTemp = result.data.careTemp,
                                diseaseName = null,
                                diseaseSeverity = null,
                                diseaseCause = null,
                                diseaseSymptoms = null,
                                diseaseTreatment = null,
                                diseasePrevention = null
                            )
                        )
                    }
                }
                is NetworkResult.ApiError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = IdentificationUiState.Error(UiText.StringResource(uiError.messageResId))
                }
                is NetworkResult.NetworkError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = IdentificationUiState.Error(UiText.StringResource(uiError.messageResId))
                }
                is NetworkResult.UnknownError -> {
                    val uiError = ErrorMapper.mapToUiError(result)
                    _uiState.value = IdentificationUiState.Error(UiText.StringResource(uiError.messageResId))
                }
            }
        }
    }
}
