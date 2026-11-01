package com.example.players.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.players.data.local.AppDatabase
import com.example.players.data.remote.ProductsApi
import com.example.players.data.repository.FavoritesRepositoryImpl
import com.example.players.data.repository.FilterSettingsRepositoryImpl
import com.example.players.data.repository.ProductsRepositoryImpl
import com.example.players.domain.repository.FavoritesRepository
import com.example.players.domain.repository.FilterSettingsRepository
import com.example.players.domain.repository.ProductsRepository
import com.example.players.domain.usecase.GetCategoriesUseCase
import com.example.players.domain.usecase.GetProductDetailsUseCase
import com.example.players.domain.usecase.GetProductsUseCase
import com.example.players.domain.usecase.ObserveFavoritesUseCase
import com.example.players.domain.usecase.ObserveFilterSettingsUseCase
import com.example.players.domain.usecase.ObserveIsFavoriteUseCase
import com.example.players.domain.usecase.SaveFilterSettingsUseCase
import com.example.players.domain.usecase.ToggleFavoriteUseCase
import com.example.players.presentation.common.FilterBadgeCache
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// DataStore для настроек фильтра (один экземпляр на процесс)
private val Context.filtersDataStore by preferencesDataStore(name = "filter_settings")

/** Простой ручной DI-контейнер: все зависимости создаются здесь и отдаются во ViewModel. */
class AppContainer(context: Context) {

    // ---------- сеть ----------
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: ProductsApi = Retrofit.Builder()
        .baseUrl("https://dummyjson.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ProductsApi::class.java)

    // ---------- локальное хранилище ----------
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "players.db"
    ).build()

    // ---------- репозитории ----------
    private val productsRepository: ProductsRepository =
        ProductsRepositoryImpl(api, database.favoriteDao(), Dispatchers.IO)

    private val favoritesRepository: FavoritesRepository =
        FavoritesRepositoryImpl(database.favoriteDao(), Dispatchers.IO)

    private val filterSettingsRepository: FilterSettingsRepository =
        FilterSettingsRepositoryImpl(context.applicationContext.filtersDataStore)

    // ---------- общий кэш бейджа (один экземпляр на экран списка и экран настроек) ----------
    val filterBadgeCache = FilterBadgeCache()

    // ---------- use cases ----------
    val getProductsUseCase = GetProductsUseCase(productsRepository)
    val getProductDetailsUseCase = GetProductDetailsUseCase(productsRepository)
    val getCategoriesUseCase = GetCategoriesUseCase(productsRepository)

    val observeFilterSettingsUseCase = ObserveFilterSettingsUseCase(filterSettingsRepository)
    val saveFilterSettingsUseCase = SaveFilterSettingsUseCase(filterSettingsRepository)

    val observeFavoritesUseCase = ObserveFavoritesUseCase(favoritesRepository)
    val observeIsFavoriteUseCase = ObserveIsFavoriteUseCase(favoritesRepository)
    val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoritesRepository)
}
