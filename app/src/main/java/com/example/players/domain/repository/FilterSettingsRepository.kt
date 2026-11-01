package com.example.players.domain.repository

import com.example.players.domain.model.FilterSettings
import kotlinx.coroutines.flow.Flow

interface FilterSettingsRepository {
    val settings: Flow<FilterSettings>
    suspend fun save(settings: FilterSettings)
}
