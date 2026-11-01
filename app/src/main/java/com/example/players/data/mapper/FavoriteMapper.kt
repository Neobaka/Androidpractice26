package com.example.players.data.mapper

import com.example.players.data.local.FavoriteEntity
import com.example.players.domain.model.Product
import com.example.players.domain.model.Review
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type
private val reviewListType = object : TypeToken<List<Review>>() {}.type

fun Product.toEntity() = FavoriteEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    availability = availability,
    shipping = shipping,
    warranty = warranty,
    returnPolicy = returnPolicy,
    thumbnail = thumbnail,
    tagsJson = gson.toJson(tags),
    imagesJson = gson.toJson(images),
    reviewsJson = gson.toJson(reviews),
    addedAt = System.currentTimeMillis()
)

fun FavoriteEntity.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    tags = parseList<String>(tagsJson, stringListType),
    availability = availability,
    shipping = shipping,
    warranty = warranty,
    returnPolicy = returnPolicy,
    thumbnail = thumbnail,
    images = parseList<String>(imagesJson, stringListType),
    reviews = parseList<Review>(reviewsJson, reviewListType)
)

private fun <T> parseList(json: String, type: java.lang.reflect.Type): List<T> =
    runCatching { gson.fromJson<List<T>>(json, type) }.getOrNull().orEmpty()
