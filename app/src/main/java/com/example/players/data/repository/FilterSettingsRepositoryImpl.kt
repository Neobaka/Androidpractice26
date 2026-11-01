package com.example.players.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.players.domain.model.FilterSettings
import com.example.players.domain.model.SortBy
import com.example.players.domain.model.SortOrder
import com.example.players.domain.repository.FilterSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class FilterSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : FilterSettingsRepository {

    override val settings: Flow<FilterSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            FilterSettings(
                category = prefs[KEY_CATEGORY],
                sortBy = prefs[KEY_SORT_BY].toEnum(SortBy.NONE),
                order = prefs[KEY_ORDER].toEnum(SortOrder.ASC)
            )
        }

    override suspend fun save(settings: FilterSettings) {
        val category = settings.category
        dataStore.edit { prefs ->
            if (category == null) prefs.remove(KEY_CATEGORY) else prefs[KEY_CATEGORY] = category
            prefs[KEY_SORT_BY] = settings.sortBy.name
            prefs[KEY_ORDER] = settings.order.name
        }
    }

    private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
        this?.let { name -> runCatching { enumValueOf<E>(name) }.getOrNull() } ?: default

    private companion object {
        val KEY_CATEGORY = stringPreferencesKey("category")
        val KEY_SORT_BY = stringPreferencesKey("sort_by")
        val KEY_ORDER = stringPreferencesKey("order")
    }
}
