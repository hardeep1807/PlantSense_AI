package com.plantsense.ai.presentation.model

import androidx.compose.runtime.Immutable
import com.plantsense.ai.R
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.model.isHealthyDiagnosis
import com.plantsense.ai.presentation.util.UiText

@Immutable
data class ScanCardUiModel(
    val id: Int,
    val type: ScanType,
    val imagePath: String,
    val title: UiText,
    val timestamp: Long,
    val isHealthy: Boolean,
    val confidencePercent: Int?
)

fun ScanHistoryItem.toUiModel(): ScanCardUiModel {
    val isHealthy = isHealthyDiagnosis
    val title = if (type == ScanType.IDENTIFICATION) {
        plantName?.let(UiText::DynamicString)
            ?: UiText.StringResource(R.string.unknown_plant)
    } else {
        diseaseName?.let(UiText::DynamicString)
            ?: UiText.StringResource(R.string.healthy)
    }
    val confidencePercent = if (type == ScanType.IDENTIFICATION) {
        ((confidence ?: 0.85) * 100).toInt()
    } else null

    return ScanCardUiModel(
        id = id,
        type = type,
        imagePath = imageUrl,
        title = title,
        timestamp = timestamp,
        isHealthy = isHealthy,
        confidencePercent = confidencePercent
    )
}
