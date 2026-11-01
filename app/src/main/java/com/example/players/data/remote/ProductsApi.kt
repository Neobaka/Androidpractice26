package com.example.players.data.remote

import com.example.players.data.remote.dto.ProductDto
import com.example.players.data.remote.dto.ProductsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductsApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String?,
        @Query("order") order: String?
    ): ProductsResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String?,
        @Query("order") order: String?
    ): ProductsResponseDto

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String?,
        @Query("order") order: String?
    ): ProductsResponseDto

    @GET("products/category-list")
    suspend fun getCategories(): List<String>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto
}
