package com.example.players.presentation.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.players.App
import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.SortBy
import com.example.players.domain.model.SortOrder
import com.example.players.domain.usecase.GetCategoriesUseCase
import com.example.players.domain.usecase.ObserveFilterSettingsUseCase
import com.example.players.domain.usecase.SaveFilterSettingsUseCase
import com.example.players.presentation.common.FilterBadgeCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiltersUiState(
    val draft: FilterSettings = FilterSettings(),
    val categories: List<String> = FALLBACK_CATEGORIES
)

class FiltersViewModel(
    private val observeFilters: ObserveFilterSettingsUseCase,
    private val saveFilters: SaveFilterSettingsUseCase,
    getCategories: GetCategoriesUseCase,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    private val _state = MutableStateFlow(FiltersUiState())
    val state: StateFlow<FiltersUiState> = _state.asStateFlow()

    init {
        // Подставляем сохранённые настройки, чтобы форма открылась в том состоянии, в котором её оставили
        viewModelScope.launch {
            val saved = observeFilters().first()
            _state.update { it.copy(draft = saved) }
        }
        // Актуальный список категорий с сервера; если сети нет — остаётся встроенный
        viewModelScope.launch {
            getCategories().onSuccess { list ->
                if (list.isNotEmpty()) _state.update { it.copy(categories = list) }
            }
        }
    }

    fun onCategorySelected(category: String?) =
        _state.update { it.copy(draft = it.draft.copy(category = category)) }

    fun onSortSelected(sortBy: SortBy) =
        _state.update { it.copy(draft = it.draft.copy(sortBy = sortBy)) }

    fun onOrderSelected(order: SortOrder) =
        _state.update { it.copy(draft = it.draft.copy(order = order)) }

    fun reset() = _state.update { it.copy(draft = FilterSettings()) }

    /** «Готово»: сохраняем в DataStore, обновляем бейдж и закрываем экран. */
    fun apply(onDone: () -> Unit) {
        val draft = _state.value.draft
        viewModelScope.launch {
            saveFilters(draft)
            badgeCache.setVisible(!draft.isDefault)
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as App).container
                FiltersViewModel(
                    c.observeFilterSettingsUseCase,
                    c.saveFilterSettingsUseCase,
                    c.getCategoriesUseCase,
                    c.filterBadgeCache
                )
            }
        }
    }
}

private val FALLBACK_CATEGORIES = listOf(
    "beauty", "fragrances", "furniture", "groceries", "home-decoration",
    "kitchen-accessories", "laptops", "mens-shirts", "mens-shoes", "mens-watches",
    "mobile-accessories", "motorcycle", "skin-care", "smartphones", "sports-accessories",
    "sunglasses", "tablets", "tops", "vehicle", "womens-bags", "womens-dresses",
    "womens-jewellery", "womens-shoes", "womens-watches"
)
