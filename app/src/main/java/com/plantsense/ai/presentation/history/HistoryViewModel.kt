package com.plantsense.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import com.plantsense.ai.core.di.IoDispatcher
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.usecase.DeleteScanUseCase
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import com.plantsense.ai.presentation.model.HistoryFilter
import com.plantsense.ai.presentation.model.ScanCardUiModel
import com.plantsense.ai.presentation.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HistoryUiState(
    val query: String = "",
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val favoriteIds: ImmutableSet<Int> = persistentSetOf(),
    val items: ImmutableList<ScanCardUiModel> = persistentListOf()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    private val deleteScanUseCase: DeleteScanUseCase,
    private val savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)

    private val _favoriteIds = MutableStateFlow(
        savedStateHandle.get<Set<Int>>(FAVORITES_KEY).orEmpty()
    )

    fun updateFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        getScanHistoryUseCase(),
        _searchQuery,
        _selectedFilter,
        _favoriteIds
    ) { history, query, filter, favorites ->
        val items = history.asSequence()
            .filter { item ->
                query.isBlank() ||
                    item.plantName?.contains(query, ignoreCase = true) == true ||
                    item.diseaseName?.contains(query, ignoreCase = true) == true
            }
            .map(ScanHistoryItem::toUiModel)
            .filter { item ->
                when (filter) {
                    HistoryFilter.ALL -> true
                    HistoryFilter.HEALTHY -> item.type == com.plantsense.ai.domain.model.ScanType.DISEASE && item.isHealthy
                    HistoryFilter.DISEASED -> item.type == com.plantsense.ai.domain.model.ScanType.DISEASE && !item.isHealthy
                    HistoryFilter.FAVORITES -> item.id in favorites
                }
            }
            .toList()
            .toImmutableList()

        HistoryUiState(
            query = query,
            selectedFilter = filter,
            favoriteIds = favorites.toImmutableSet(),
            items = items
        )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(id: Int) {
        val updated = _favoriteIds.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }.toSet()
        _favoriteIds.value = updated
        savedStateHandle[FAVORITES_KEY] = updated
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch(ioDispatcher) {
            deleteScanUseCase(id)
        }
    }

    private companion object {
        const val FAVORITES_KEY = "favorite_scan_ids"
    }
}
