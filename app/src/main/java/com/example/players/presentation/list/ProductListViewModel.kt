package com.example.players.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.players.App
import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.Product
import com.example.players.domain.usecase.GetProductsUseCase
import com.example.players.domain.usecase.ObserveFilterSettingsUseCase
import com.example.players.presentation.common.FilterBadgeCache
import com.example.players.presentation.common.toUiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductListUiState(
    val query: String = "",
    val items: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false
)

class ProductListViewModel(
    private val getProducts: GetProductsUseCase,
    observeFilters: ObserveFilterSettingsUseCase,
    badgeCache: FilterBadgeCache
) : ViewModel() {

    private val _state = MutableStateFlow(ProductListUiState())
    val state: StateFlow<ProductListUiState> = _state.asStateFlow()

    /** Бейдж на кнопке настроек — читается из общего кэша. */
    val showFilterBadge: StateFlow<Boolean> = badgeCache.isVisible

    private var filters = FilterSettings()
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        // Настройки читаются из DataStore при старте и при каждом сохранении на экране фильтров:
        // в обоих случаях список перезагружается с актуальными фильтрами.
        viewModelScope.launch {
            var first = true
            observeFilters().collect { saved ->
                filters = saved
                if (first) {
                    badgeCache.setVisible(!saved.isDefault)
                    first = false
                }
                loadFirstPage()
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadFirstPage()
        }
    }

    fun retry() {
        if (_state.value.items.isEmpty()) {
            loadFirstPage()
        } else {
            _state.update { it.copy(error = null) }
            loadNextPage()
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoading || s.isLoadingMore || s.endReached || s.error != null || s.items.isEmpty()) return

        _state.update { it.copy(isLoadingMore = true) }
        loadJob = viewModelScope.launch {
            getProducts(s.query, filters, PAGE_SIZE, s.items.size).fold(
                onSuccess = { page ->
                    _state.update {
                        val all = it.items + page.items
                        it.copy(
                            items = all,
                            isLoadingMore = false,
                            endReached = page.items.isEmpty() || all.size >= page.total
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoadingMore = false, error = e.toUiMessage()) }
                }
            )
        }
    }

    private fun loadFirstPage() {
        loadJob?.cancel()
        val query = _state.value.query
        _state.update {
            it.copy(isLoading = true, isLoadingMore = false, error = null, items = emptyList(), endReached = false)
        }
        loadJob = viewModelScope.launch {
            getProducts(query, filters, PAGE_SIZE, 0).fold(
                onSuccess = { page ->
                    _state.update {
                        it.copy(
                            items = page.items,
                            isLoading = false,
                            endReached = page.items.size >= page.total
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUiMessage()) }
                }
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val SEARCH_DEBOUNCE_MS = 400L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as App).container
                ProductListViewModel(
                    c.getProductsUseCase,
                    c.observeFilterSettingsUseCase,
                    c.filterBadgeCache
                )
            }
        }
    }
}
