package com.example.players.domain.model

enum class SortBy { NONE, TITLE, PRICE, RATING }

enum class SortOrder { ASC, DESC }

/** Три настройки: категория, поле сортировки, порядок сортировки. */
data class FilterSettings(
    val category: String? = null,
    val sortBy: SortBy = SortBy.NONE,
    val order: SortOrder = SortOrder.ASC
) {
    /** Порядок не имеет смысла без сортировки, поэтому в проверке не участвует. */
    val isDefault: Boolean
        get() = category == null && sortBy == SortBy.NONE
}
