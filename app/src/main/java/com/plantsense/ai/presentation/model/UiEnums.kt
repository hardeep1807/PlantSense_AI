package com.plantsense.ai.presentation.model

import com.plantsense.ai.R
import androidx.annotation.StringRes

enum class CameraMode(@StringRes val labelRes: Int) {
    IDENTIFY(R.string.mode_identify),
    DIAGNOSE(R.string.mode_diagnose)
}

enum class HistoryFilter(
    @StringRes val labelRes: Int,
    @StringRes val emptyTitleRes: Int = R.string.no_scans_found,
    @StringRes val emptyDescriptionRes: Int = R.string.history_empty_description
) {
    ALL(R.string.filter_all),
    HEALTHY(R.string.filter_healthy),
    DISEASED(R.string.filter_diseased),
    FAVORITES(
        labelRes = R.string.filter_favorites,
        emptyTitleRes = R.string.no_favorites_saved,
        emptyDescriptionRes = R.string.favorites_empty_description
    )
}

enum class VigorRank(@StringRes val labelRes: Int) {
    NOVICE(R.string.rank_novice),
    EXPLORER(R.string.rank_explorer),
    SPECIALIST(R.string.rank_specialist),
    EXPERT(R.string.rank_expert)
}
