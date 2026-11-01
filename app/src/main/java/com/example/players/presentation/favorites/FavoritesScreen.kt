package com.example.players.presentation.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.players.presentation.common.AppOrange
import com.example.players.presentation.common.LoadingView
import com.example.players.presentation.common.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onProductClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory)
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val list = favorites

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppOrange,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                list == null -> LoadingView()
                list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Пока ничего нет в избранном")
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            trailing = {
                                IconButton(onClick = { viewModel.remove(product) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Убрать из избранного")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
