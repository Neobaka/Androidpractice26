package com.example.players.domain.usecase

import com.example.players.domain.model.FilterSettings
import com.example.players.domain.repository.FilterSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveFilterSettingsUseCase(private val repository: FilterSettingsRepository) {
    operator fun invoke(): Flow<FilterSettings> = repository.settings
}

class SaveFilterSettingsUseCase(private val repository: FilterSettingsRepository) {
    suspend operator fun invoke(settings: FilterSettings) = repository.save(settings)
}
