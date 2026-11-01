package com.example.players.domain.repository

import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.Product
import com.example.players.domain.model.ProductsPage

interface ProductsRepository {
    suspend fun getProducts(
        query: String,
        filters: FilterSettings,
        limit: Int,
        skip: Int
    ): Result<ProductsPage>

    suspend fun getProduct(id: Int): Result<Product>

    suspend fun getCategories(): Result<List<String>>
}
