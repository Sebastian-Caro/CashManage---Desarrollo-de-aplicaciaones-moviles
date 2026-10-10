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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
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
    val metodosPago by viewModel.allMetodosPago.collectAsStateWithLifecycle()

    var showInitialBudgetDialog by remember { mutableStateOf(false) }
    var initialBudgetError by remember { mutableStateOf<String?>(null) }
    var newSourceName by remember { mutableStateOf("") }
    var pendingSourceName by remember { mutableStateOf<String?>(null) }
    var initialBudgetSaving by remember { mutableStateOf(false) }
    val initialBalances = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(metodosPago) {
        metodosPago.forEach {
            if (it.id !in initialBalances) initialBalances[it.id] = "0"
        }
        initialBalances.keys.toList().filter { id -> metodosPago.none { it.id == id } }
            .forEach(initialBalances::remove)
        pendingSourceName?.let { pendingName ->
            if (metodosPago.any { it.nombre.equals(pendingName, ignoreCase = true) }) {
                pendingSourceName = null
            }
        }
    }
    LaunchedEffect(presupuesto?.configurado) {
        showInitialBudgetDialog = presupuesto != null && !presupuesto!!.configurado
    }

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
            // Budget status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = ForestOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Presupuesto inicial",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ForestOnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
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
                            text = "Manual",
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
                            text = "DINERO DISPONIBLE PARA GASTAR",
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
                    val totalInicial = presupuesto?.metaTotal ?: 0.0
                    val limiteGastable = if (presupuesto?.configurado == true) totalInicial * 0.8 else 0.0
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
                            text = "Límite gastable: ${CashManagerViewModel.formatCurrency(limiteGastable)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total inicial",
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestOnPrimaryContainer)
                        )
                        Text(
                            text = CashManagerViewModel.formatCurrency(totalInicial),
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestOnPrimaryContainer)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reserva protegida (20%)",
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestOnPrimaryContainer)
                        )
                        Text(
                            text = CashManagerViewModel.formatCurrency(totalInicial * 0.2),
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestOnPrimaryContainer)
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
                                .fillMaxWidth((porcentajeConsumido / 100f).coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSecondaryFixed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.ADD_EXPENSE) },
                    shape = RoundedCornerShape(9999.dp),
                    color = ForestSurfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        text = "Aún no hay movimientos registrados.",
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

    if (showInitialBudgetDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Organicemos tu presupuesto",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ingresa cuánto dinero tienes actualmente en cada fuente. El 80% quedará disponible para gastos y el 20% se reservará.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ForestOnSurfaceVariant)
                    )
                    metodosPago.forEach { metodo ->
                        OutlinedTextField(
                            value = initialBalances[metodo.id].orEmpty(),
                            onValueChange = { input ->
                                if (input.all(Char::isDigit)) initialBalances[metodo.id] = input
                            },
                            label = { Text(metodo.nombre) },
                            prefix = { Text("$") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_initial_balance_${metodo.id}")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSourceName,
                            onValueChange = { newSourceName = it },
                            label = { Text("Otra fuente (ej. Nequi)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                val name = newSourceName.trim()
                                if (metodosPago.any { it.nombre.equals(name, ignoreCase = true) }) {
                                    initialBudgetError = "Ya existe una fuente con ese nombre."
                                } else if (name.isNotEmpty() && pendingSourceName == null) {
                                    initialBudgetError = null
                                    pendingSourceName = name
                                    viewModel.addPaymentMethod(name)
                                    newSourceName = ""
                                }
                            },
                            enabled = newSourceName.isNotBlank() &&
                                !initialBudgetSaving &&
                                pendingSourceName == null
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Agregar")
                        }
                    }
                    val totalIngresado = metodosPago.sumOf {
                        initialBalances[it.id]?.toDoubleOrNull() ?: 0.0
                    }
                    Text(
                        text = "Total: ${CashManagerViewModel.formatCurrency(totalIngresado)} · " +
                            "Para gastar (80%): ${CashManagerViewModel.formatCurrency(totalIngresado * 0.8)} · " +
                            "Reserva: ${CashManagerViewModel.formatCurrency(totalIngresado * 0.2)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ForestPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    initialBudgetError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balances = metodosPago.associate { metodo ->
                            metodo.id to (initialBalances[metodo.id]?.toDoubleOrNull() ?: -1.0)
                        }
                        if (balances.values.any { it < 0.0 }) {
                            initialBudgetError = "Ingresa un saldo válido para cada fuente."
                            return@Button
                        }
                        initialBudgetSaving = true
                        viewModel.configureInitialBudget(balances) { success, message ->
                            initialBudgetSaving = false
                            if (success) {
                                showInitialBudgetDialog = false
                            } else {
                                initialBudgetError = message
                            }
                        }
                    },
                    enabled = !initialBudgetSaving && pendingSourceName == null && metodosPago.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary)
                ) {
                    Text(
                        if (initialBudgetSaving) "Guardando..." else "Guardar presupuesto",
                        color = ForestOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
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
