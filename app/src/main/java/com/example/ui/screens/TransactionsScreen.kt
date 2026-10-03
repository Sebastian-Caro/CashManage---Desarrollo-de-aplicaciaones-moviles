package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Movimiento
import com.example.ui.components.CashManagerBottomBar
import com.example.ui.components.CashManagerTopHeader
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestError
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestOutlineVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun TransactionsScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val allMovimientos by viewModel.allMovimientos.collectAsStateWithLifecycle()
    val totalGastos by viewModel.totalGastos.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredList = allMovimientos.filter {
        (searchQuery.isBlank() || it.descripcion.contains(searchQuery, ignoreCase = true) || it.categoriaNombre.contains(searchQuery, ignoreCase = true) || it.metodoPagoNombre.contains(searchQuery, ignoreCase = true)) &&
        (selectedCategoryFilter == null || it.categoriaNombre.equals(selectedCategoryFilter, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ForestBackground,
        bottomBar = {
            CashManagerBottomBar(
                currentScreen = AppScreen.TRANSACTIONS,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.ADD_EXPENSE) },
                containerColor = ForestPrimaryContainer,
                contentColor = ForestOnPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Gasto"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CashManagerTopHeader(
                title = "Cash Manager",
                subtitle = "Movimientos",
                avatarUrl = user?.avatarUrl,
                showBackButton = true,
                onBackClick = { viewModel.navigateBack() },
                onAvatarClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header Total Summary
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ForestSurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL GASTADO EN EL PERIODO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestOnSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CashManagerViewModel.formatCurrency(totalGastos),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestPrimary
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSurfaceContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${allMovimientos.size} registros",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nota, categoría o método...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ForestOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(9999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ForestSurfaceContainerLowest,
                        unfocusedContainerColor = ForestSurfaceContainerLowest,
                        focusedBorderColor = ForestPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("input_search_transactions")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // List of transactions
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron movimientos.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ForestOnSurfaceVariant)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList, key = { it.id }) { mov ->
                            TransactionDetailedRow(
                                mov = mov,
                                onDelete = { viewModel.deleteMovimiento(mov.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailedRow(
    mov: Movimiento,
    onDelete: () -> Unit
) {
    val icon = when (mov.categoriaNombre) {
        "Alimentación" -> Icons.Default.Restaurant
        "Academia", "Materiales" -> Icons.Default.AutoStories
        "Transporte" -> Icons.Default.DirectionsSubway
        else -> Icons.Default.MoreHoriz
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ForestSurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ForestSurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ForestPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mov.descripcion,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mov.fechaTexto,
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(ForestOutlineVariant)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${mov.categoriaNombre} (${mov.metodoPagoNombre})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ForestPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${CashManagerViewModel.formatCurrency(mov.monto)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestOnSurface
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = ForestError.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
