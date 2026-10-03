package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
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
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSecondaryFixedDim
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun AddExpenseScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val metodosPago by viewModel.allMetodosPago.collectAsStateWithLifecycle()
    val saldoDisponible by viewModel.saldoDisponible.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf("GASTO") }
    var selectedMetodoId by remember { mutableStateOf(0L) }
    var noteText by remember { mutableStateOf("") }

    val metodoSeleccionado = metodosPago.firstOrNull { it.pkMetodo == selectedMetodoId }
        ?: metodosPago.firstOrNull()
    val metodoId = metodoSeleccionado?.pkMetodo ?: 0L

    val scrollState = rememberScrollState()
    val metodoScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        CashManagerTopHeader(
            title = "Cash Manager",
            subtitle = if (selectedTipo == "INGRESO") "Nuevo ingreso" else "Nuevo gasto",
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
                    Text(
                        text = if (selectedTipo == "INGRESO") "¿Cuánto recibiste?" else "¿Cuánto gastaste?",
                        style = MaterialTheme.typography.labelMedium.copy(color = ForestOnSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary,
                                fontSize = 32.sp
                            )
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) amountText = input
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
                            placeholder = { Text("0", fontSize = 32.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("input_expense_amount")
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ForestSecondaryFixedDim)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Presupuesto restante: ${CashManagerViewModel.formatCurrency(saldoDisponible)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ForestSurfaceContainerLowest)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Tipo de movimiento",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("GASTO" to "Gasto", "INGRESO" to "Ingreso").forEach { (value, label) ->
                            val selected = selectedTipo == value
                            Surface(
                                onClick = { selectedTipo = value },
                                shape = RoundedCornerShape(9999.dp),
                                color = if (selected) ForestPrimaryContainer else ForestSurfaceContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selected) ForestOnPrimary else ForestOnSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Cuenta / método de pago",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(metodoScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        metodosPago.forEach { metodo ->
                            val selected = metodo.pkMetodo == metodoId
                            val icon = when {
                                metodo.nombre.contains("Efectivo", ignoreCase = true) -> Icons.Default.Payments
                                metodo.nombre.contains("Nu", ignoreCase = true) -> Icons.Default.CreditCard
                                else -> Icons.Default.AccountBalanceWallet
                            }
                            Surface(
                                onClick = { selectedMetodoId = metodo.pkMetodo },
                                shape = RoundedCornerShape(9999.dp),
                                color = if (selected) ForestPrimaryContainer else ForestSurfaceContainer,
                                modifier = Modifier.height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) ForestOnPrimary else ForestPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = metodo.nombre,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selected) ForestOnPrimary else ForestOnSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Nota",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
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
            }

            Button(
                onClick = {
                    viewModel.addMovimiento(
                        monto = amountText.toDoubleOrNull() ?: 0.0,
                        tipo = selectedTipo,
                        metodoId = metodoId,
                        nota = noteText,
                        onSuccess = { viewModel.navigateBack() }
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
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedTipo == "INGRESO") "Guardar ingreso" else "Guardar gasto",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
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
