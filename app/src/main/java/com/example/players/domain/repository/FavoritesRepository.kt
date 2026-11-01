package com.example.players.domain.repository

import com.example.players.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeIsFavorite(id: Int): Flow<Boolean>

    /** Добавляет в избранное, а если уже там — удаляет. */
    suspend fun toggle(product: Product)
}
