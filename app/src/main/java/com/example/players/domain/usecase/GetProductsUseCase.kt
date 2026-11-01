package com.example.players.domain.usecase

import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.ProductsPage
import com.example.players.domain.repository.ProductsRepository

class GetProductsUseCase(private val repository: ProductsRepository) {
    suspend operator fun invoke(
        query: String,
        filters: FilterSettings,
        limit: Int,
        skip: Int
    ): Result<ProductsPage> = repository.getProducts(query.trim(), filters, limit, skip)
}
