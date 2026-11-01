package com.example.players.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Хранится весь товар целиком, чтобы карточка открывалась без интернета.
 * Списки (теги, картинки, отзывы) лежат в виде JSON-строк.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val availability: String,
    val shipping: String,
    val warranty: String,
    val returnPolicy: String,
    val thumbnail: String,
    val tagsJson: String,
    val imagesJson: String,
    val reviewsJson: String,
    val addedAt: Long
)
