package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Movimiento
import com.example.ui.components.CashManagerBottomBar
import com.example.ui.components.CashManagerTopHeader
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnPrimaryContainer
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestOutlineVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerHigh
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.theme.ForestSurfaceVariant
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun HomeScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val presupuesto by viewModel.activePresupuesto.collectAsStateWithLifecycle()
    val saldoDisponible by viewModel.saldoDisponible.collectAsStateWithLifecycle()
    val porcentajeConsumido by viewModel.porcentajeConsumido.collectAsStateWithLifecycle()
    val recentMovimientos by viewModel.recentMovimientos.collectAsStateWithLifecycle()

    var showAdjustBudgetDialog by remember { mutableStateOf(false) }
    var newBudgetLimitText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        // Top Header
        CashManagerTopHeader(
            title = "Cash Manager",
            subtitle = "Inicio",
            avatarUrl = user?.avatarUrl,
            onAvatarClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Period selector bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.showToast("Periodo activo: Mayo 2025")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ForestOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Este mes (${presupuesto?.periodo ?: "Mayo"})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ForestOnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ForestOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ForestSurfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ForestSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Al día",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Hero Protagonist Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(ForestPrimaryContainer)
                    .padding(20.dp)
                    .testTag("hero_balance_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SALDO DISPONIBLE DEL MES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = ForestOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CashManagerViewModel.formatCurrency(saldoDisponible),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Disponible para gastos libres sin tocar ahorros",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ForestOnPrimaryContainer.copy(alpha = 0.9f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar & limits
                    val meta = presupuesto?.metaTotal ?: 500000.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consumido: $porcentajeConsumido%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Meta: ${CashManagerViewModel.formatCurrency(meta)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(ForestSurfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((porcentajeConsumido / 100f).coerceIn(0.01f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSecondaryFixed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions: [ Registrar gasto ] [ Ajustar límite ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.ADD_EXPENSE) },
                    shape = RoundedCornerShape(9999.dp),
                    color = ForestSurfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_quick_register_expense")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = ForestPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar gasto",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ForestOnSurface
                            )
                        )
                    }
                }

                Surface(
                    onClick = {
                        newBudgetLimitText = (presupuesto?.metaTotal ?: 500000.0).toLong().toString()
                        showAdjustBudgetDialog = true
                    },
                    shape = RoundedCornerShape(9999.dp),
                    color = ForestSurfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_adjust_limit")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ForestPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ajustar límite",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ForestOnSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Últimos movimientos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos movimientos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestOnSurface
                    )
                )
                Text(
                    text = "${recentMovimientos.size} recientes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForestOnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Movements list
            if (recentMovimientos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ForestSurfaceContainerLowest)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay movimientos registrados este mes.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ForestOnSurfaceVariant)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentMovimientos.forEach { mov ->
                        MovementItemRow(mov = mov)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Full Breakdown Action Button
            Button(
                onClick = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestPrimaryContainer,
                    contentColor = ForestOnPrimary
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_view_full_breakdown")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ver desglose completo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Navigation Bar
        CashManagerBottomBar(
            currentScreen = AppScreen.HOME,
            onNavigate = { viewModel.navigateTo(it) }
        )
    }

    if (showAdjustBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustBudgetDialog = false },
            title = {
                Text(
                    text = "Ajustar Límite de Presupuesto",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Define tu meta mensual para gastos universitarios (sin tocar tus ahorros):",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ForestOnSurfaceVariant)
                    )
                    OutlinedTextField(
                        value = newBudgetLimitText,
                        onValueChange = { newBudgetLimitText = it },
                        label = { Text("Monto límite ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_budget_limit")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newBudgetLimitText.toDoubleOrNull() ?: 500000.0
                        viewModel.updatePresupuestoLimit(amount)
                        showAdjustBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary)
                ) {
                    Text("Guardar", color = ForestOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustBudgetDialog = false }) {
                    Text("Cancelar", color = ForestOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun MovementItemRow(
    mov: Movimiento,
    onDelete: (() -> Unit)? = null
) {
    val icon = when (mov.categoriaNombre) {
        "Alimentación" -> Icons.Default.Restaurant
        "Academia", "Materiales" -> Icons.Default.AutoStories
        "Transporte" -> Icons.Default.DirectionsSubway
        else -> Icons.Default.MoreHoriz
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
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
                            fontWeight = FontWeight.SemiBold,
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
                            text = mov.categoriaNombre,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ForestPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Text(
                text = "-${CashManagerViewModel.formatCurrency(mov.monto)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestOnSurface
                )
            )
        }
    }
}
