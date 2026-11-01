package com.example.players.domain.usecase

import com.example.players.domain.repository.ProductsRepository

class GetCategoriesUseCase(private val repository: ProductsRepository) {
    suspend operator fun invoke(): Result<List<String>> = repository.getCategories()
}
