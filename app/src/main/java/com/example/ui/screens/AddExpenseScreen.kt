package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CashManagerTopHeader
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSecondaryFixed
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestOutline
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSecondaryFixedDim
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerHigh
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun AddExpenseScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val categorias by viewModel.allCategorias.collectAsStateWithLifecycle()
    val metodosPago by viewModel.allMetodosPago.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("12500") }
    var selectedCategory by remember { mutableStateOf("Alimentación") }
    var selectedPaymentMethod by remember { mutableStateOf("Digital") }
    var noteText by remember { mutableStateOf("Café y sándwich con grupo de cálculo") }
    var dateTimeText by remember { mutableStateOf("Hoy, 2:15 PM") }

    val scrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        CashManagerTopHeader(
            title = "Cash Manager",
            subtitle = "Nuevo Gasto",
            avatarUrl = user?.avatarUrl,
            showBackButton = true,
            onBackClick = { viewModel.navigateBack() },
            onAvatarClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Amount Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ForestSurfaceContainerLowest)
                    .padding(20.dp)
                    .testTag("hero_input_card"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = ForestPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "¿Cuánto gastaste?",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ForestOnSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input amount
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary,
                                fontSize = 32.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    amountText = input
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface,
                                fontSize = 32.sp
                            ),
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("input_expense_amount")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ForestSecondaryFixedDim)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Presupuesto semanal restante: $ 48.200",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ForestOnSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Form Fields Container Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ForestSurfaceContainerLowest)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Category selector chips
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categoría",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                            Text(
                                text = "Obligatorio",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestOnSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category horizontal chips list
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(categoryScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val availableCats = if (categorias.isNotEmpty()) {
                                categorias.map { it.nombre }
                            } else {
                                listOf("Alimentación", "Transporte", "Materiales", "Ocio", "Otros")
                            }

                            availableCats.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                val catIcon = when (cat) {
                                    "Alimentación" -> Icons.Default.Restaurant
                                    "Transporte" -> Icons.Default.DirectionsBus
                                    "Materiales", "Academia" -> Icons.Default.MenuBook
                                    "Ocio" -> Icons.Default.SportsEsports
                                    else -> Icons.Default.MoreHoriz
                                }

                                Surface(
                                    onClick = { selectedCategory = cat },
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) ForestPrimaryContainer else ForestSurfaceContainer,
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) ForestOnPrimary else ForestOnSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) ForestOnPrimary else ForestOnSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Date & Time Field
                    Column {
                        Text(
                            text = "Fecha y hora",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSurfaceContainerLow)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = ForestPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateTimeText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = ForestOnSurface
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = "Cambiar fecha",
                                    tint = ForestOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Detail / Note Field
                    Column {
                        Text(
                            text = "Detalle o nota",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("Ej. Café y sándwich con grupo de cálculo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = ForestOnSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ForestSurfaceContainerLow,
                                unfocusedContainerColor = ForestSurfaceContainerLow,
                                focusedBorderColor = ForestPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_expense_note")
                        )
                    }

                    // Payment Method selector chips
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Método de pago",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                            Text(
                                text = "Opcional",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestOnSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Efectivo", "Digital", "Débito").forEach { method ->
                                val isSelected = method == selectedPaymentMethod
                                val pIcon = when (method) {
                                    "Efectivo" -> Icons.Default.Payments
                                    "Digital" -> Icons.Default.AccountBalanceWallet
                                    else -> Icons.Default.CreditCard
                                }

                                Surface(
                                    onClick = { selectedPaymentMethod = method },
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) ForestPrimaryContainer else ForestSurfaceContainer,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = pIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) ForestOnPrimary else ForestPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = method,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) ForestOnPrimary else ForestOnSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Friendly suggestion banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ForestSurfaceContainerHigh.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ForestSecondaryFixed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = ForestOnSecondaryFixed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "¡Vas bien! Mantienes el 74% de tu presupuesto de comida esta quincena.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ForestOnSurfaceVariant,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        viewModel.addGasto(
                            monto = amount,
                            categoria = selectedCategory,
                            metodoPago = selectedPaymentMethod,
                            nota = noteText,
                            onSuccess = {
                                viewModel.navigateBack()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestPrimaryContainer,
                        contentColor = ForestOnPrimary
                    ),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_expense")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar Gasto",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.navigateBack() },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_cancel_expense")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = ForestPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
