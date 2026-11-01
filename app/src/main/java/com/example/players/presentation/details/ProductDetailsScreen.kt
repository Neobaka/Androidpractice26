package com.example.players.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.players.domain.model.Product
import com.example.players.domain.model.Review
import com.example.players.presentation.common.AppOrange
import com.example.players.presentation.common.ErrorView
import com.example.players.presentation.common.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    onBack: () -> Unit,
    viewModel: ProductDetailsViewModel = viewModel(factory = ProductDetailsViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карточка", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state is DetailsUiState.Success) {
                        IconButton(onClick = viewModel::onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppOrange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                DetailsUiState.Loading -> LoadingView()
                is DetailsUiState.Error -> ErrorView(message = s.message, onRetry = viewModel::load)
                is DetailsUiState.Success -> DetailsContent(
                    product = s.product,
                    reviewsExpanded = s.reviewsExpanded,
                    onToggleReviews = viewModel::toggleReviews
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    product: Product,
    reviewsExpanded: Boolean,
    onToggleReviews: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Header(product)
        Spacer(Modifier.height(16.dp))

        if (product.images.size > 1) {
            Gallery(product.images)
            Spacer(Modifier.height(16.dp))
        }

        if (product.description.isNotBlank()) {
            Text(product.description)
            Spacer(Modifier.height(16.dp))
        }

        Text("Информация", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        InfoRow("Категория", product.category)
        InfoRow("Наличие", product.availability)
        InfoRow("Доставка", product.shipping)
        InfoRow("Гарантия", product.warranty)
        InfoRow("Возврат", product.returnPolicy)

        Spacer(Modifier.height(16.dp))
        StatsSection(product)

        Spacer(Modifier.height(24.dp))
        ReviewsSection(product.reviews, reviewsExpanded, onToggleReviews)
    }
}

@Composable
private fun Header(product: Product) {
    ConstraintLayout(Modifier.fillMaxWidth()) {
        val (image, title, brand, price, discount) = createRefs()

        AsyncImage(
            model = product.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEEEEE))
                .constrainAs(image) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )

        Text(
            text = product.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(image.end, margin = 12.dp)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = product.brand ?: product.category,
            color = Color.Gray,
            modifier = Modifier.constrainAs(brand) {
                start.linkTo(title.start)
                top.linkTo(title.bottom, margin = 4.dp)
            }
        )

        Text(
            text = "$%.2f".format(product.price),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(price) {
                start.linkTo(title.start)
                bottom.linkTo(image.bottom)
            }
        )

        if (product.discountPercentage > 0) {
            Text(
                text = "-%.0f%%".format(product.discountPercentage),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .constrainAs(discount) {
                        end.linkTo(parent.end)
                        bottom.linkTo(image.bottom)
                    }
                    .background(Color(0xFFD71E0A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun Gallery(images: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(images) { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEEEEE))
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(96.dp))
        Text(value, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsSection(product: Product) {
    Text("Характеристики", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Chip("★ %.1f".format(product.rating), Color(0xFFD4B106))
        Chip("${product.stock} в наличии", Color(0xFF757575))
        product.tags.forEach { Chip(it, Color(0xFF00B8A9)) }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun ReviewsSection(reviews: List<Review>, expanded: Boolean, onToggle: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Отзывы (${reviews.size})", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (reviews.isEmpty()) Text("Отзывов пока нет", color = Color.Gray)
                reviews.forEach { review ->
                    Column {
                        Row {
                            Text(
                                review.reviewerName,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text("★ ${review.rating}")
                        }
                        Text(review.comment)
                        Text(review.date, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
