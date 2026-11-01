package com.example.players.data.repository

import com.example.players.data.local.FavoriteDao
import com.example.players.data.mapper.toDomain
import com.example.players.data.remote.ProductsApi
import com.example.players.domain.error.AppError
import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.Product
import com.example.players.domain.model.ProductsPage
import com.example.players.domain.model.SortBy
import com.example.players.domain.model.SortOrder
import com.example.players.domain.repository.ProductsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ProductsRepositoryImpl(
    private val api: ProductsApi,
    private val favoriteDao: FavoriteDao,
    private val ioDispatcher: CoroutineDispatcher
) : ProductsRepository {

    override suspend fun getProducts(
        query: String,
        filters: FilterSettings,
        limit: Int,
        skip: Int
    ): Result<ProductsPage> = safeCall {
        val sortBy = filters.sortBy.toApi()
        val order = if (sortBy != null) filters.order.toApi() else null
        val category = filters.category

        val response = when {
            // Поиск по тексту приоритетнее категории: API не умеет комбинировать q и category
            query.isNotBlank() -> api.searchProducts(query, limit, skip, sortBy, order)
            category != null -> api.getProductsByCategory(category, limit, skip, sortBy, order)
            else -> api.getProducts(limit, skip, sortBy, order)
        }
        response.toDomain()
    }

    override suspend fun getProduct(id: Int): Result<Product> {
        val remote = safeCall { api.getProduct(id).toDomain() ?: throw AppError.Unknown() }
        if (remote.isSuccess) return remote

        // Нет сети — пробуем отдать сохранённый в избранном товар
        val local = withContext(ioDispatcher) { favoriteDao.getById(id) }?.toDomain()
        return if (local != null) Result.success(local) else remote
    }

    override suspend fun getCategories(): Result<List<String>> = safeCall { api.getCategories() }

    private fun SortBy.toApi(): String? = when (this) {
        SortBy.NONE -> null
        SortBy.TITLE -> "title"
        SortBy.PRICE -> "price"
        SortBy.RATING -> "rating"
    }

    private fun SortOrder.toApi(): String = when (this) {
        SortOrder.ASC -> "asc"
        SortOrder.DESC -> "desc"
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(ioDispatcher) {
            try {
                Result.success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                Result.failure(e)
            } catch (e: IOException) {
                Result.failure(AppError.NoInternet(e))
            } catch (e: HttpException) {
                Result.failure(AppError.Server(e.code()))
            } catch (e: Exception) {
                Result.failure(AppError.Unknown(e))
            }
        }
}
