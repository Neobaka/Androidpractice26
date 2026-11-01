package com.example.players.domain.usecase

import com.example.players.domain.model.Product
import com.example.players.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAll()
}

class ObserveIsFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(id: Int): Flow<Boolean> = repository.observeIsFavorite(id)
}

class ToggleFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(product: Product) = repository.toggle(product)
}
