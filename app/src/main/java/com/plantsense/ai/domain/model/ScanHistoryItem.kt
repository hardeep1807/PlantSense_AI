package com.plantsense.ai.domain.model

data class ScanHistoryItem(
    val id: Int,
    val type: ScanType,
    val imageUrl: String,
    val timestamp: Long,
    val plantName: String? = null,
    val botanicalName: String? = null,
    val confidence: Double? = null,
    val description: String? = null,
    val careLight: String? = null,
    val careWater: String? = null,
    val careTemp: String? = null,
    val diseaseName: String? = null,
    val diseaseSeverity: String? = null,
    val diseaseCause: String? = null,
    val diseaseSymptoms: String? = null,
    val diseaseTreatment: String? = null,
    val diseasePrevention: String? = null
)

/** Compatibility for records created before healthy diagnoses were stored with a null name. */
val ScanHistoryItem.isHealthyDiagnosis: Boolean
    get() = diseaseName.isNullOrEmpty() || diseaseName == LEGACY_HEALTHY_DISEASE_NAME

private const val LEGACY_HEALTHY_DISEASE_NAME = "Healthy"
