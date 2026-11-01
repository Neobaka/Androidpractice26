package com.example.players.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.players.App
import com.example.players.domain.model.Product
import com.example.players.domain.usecase.ObserveFavoritesUseCase
import com.example.players.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase
) : ViewModel() {

    /** null — данные из базы ещё читаются. */
    val favorites: StateFlow<List<Product>?> = observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(product: Product) {
        viewModelScope.launch { toggleFavorite(product) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as App).container
                FavoritesViewModel(c.observeFavoritesUseCase, c.toggleFavoriteUseCase)
            }
        }
    }
}
