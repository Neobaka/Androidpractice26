package com.example.players.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Кэш в памяти: нужно ли показывать бейдж «фильтры применены».
 * Общий для экрана списка (читает) и экрана настроек (пишет). Создаётся через DI.
 * Долгосрочно не хранится — при новом запуске список сам проставит значение по сохранённым настройкам.
 */
class FilterBadgeCache {
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    fun setVisible(visible: Boolean) {
        _isVisible.value = visible
    }
}
