package com.example.players.presentation.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.players.domain.model.SortBy
import com.example.players.domain.model.SortOrder
import com.example.players.presentation.common.AppOrange
import com.example.players.presentation.common.categoryTitle

private fun SortBy.title() = when (this) {
    SortBy.NONE -> "Без сортировки"
    SortBy.TITLE -> "По названию"
    SortBy.PRICE -> "По цене"
    SortBy.RATING -> "По рейтингу"
}

private fun SortOrder.title() = when (this) {
    SortOrder.ASC -> "По возрастанию"
    SortOrder.DESC -> "По убыванию"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onClose: () -> Unit,
    viewModel: FiltersViewModel = viewModel(factory = FiltersViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft = state.draft

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppOrange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- 1. Категория ----------
            Text("Категория", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            CategoryPicker(
                selected = draft.category,
                categories = state.categories,
                onSelect = viewModel::onCategorySelected
            )
            Text(
                "Если в списке введён текст поиска, категория не применяется",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // ---------- 2. Сортировка ----------
            Spacer(Modifier.height(24.dp))
            Text("Сортировка", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            SortBy.entries.forEach { option ->
                RadioRow(
                    title = option.title(),
                    selected = draft.sortBy == option,
                    onClick = { viewModel.onSortSelected(option) }
                )
            }

            // ---------- 3. Порядок ----------
            Spacer(Modifier.height(24.dp))
            Text("Порядок", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            SortOrder.entries.forEach { option ->
                RadioRow(
                    title = option.title(),
                    selected = draft.order == option,
                    enabled = draft.sortBy != SortBy.NONE,
                    onClick = { viewModel.onOrderSelected(option) }
                )
            }

            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.weight(1f)
                ) { Text("Сбросить", color = AppOrange) }

                Button(
                    onClick = { viewModel.apply(onDone = onClose) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppOrange)
                ) { Text("Готово") }
            }
        }
    }
}

@Composable
private fun CategoryPicker(
    selected: String?,
    categories: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selected?.let(::categoryTitle) ?: "Все категории",
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Все категории") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(categoryTitle(category)) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = AppOrange)
        )
        Spacer(Modifier.width(8.dp))
        Text(title, color = if (enabled) Color.Black else Color.Gray)
    }
}
