package com.example.players.data.repository

import com.example.players.data.local.FavoriteDao
import com.example.players.data.mapper.toDomain
import com.example.players.data.mapper.toEntity
import com.example.players.domain.model.Product
import com.example.players.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
    private val ioDispatcher: CoroutineDispatcher
) : FavoritesRepository {

    override fun observeAll(): Flow<List<Product>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeIsFavorite(id: Int): Flow<Boolean> = dao.observeIsFavorite(id)

    override suspend fun toggle(product: Product) = withContext(ioDispatcher) {
        if (dao.getById(product.id) != null) {
            dao.deleteById(product.id)
        } else {
            dao.insert(product.toEntity())
        }
    }
}
