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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.CashManagerBottomBar
import com.example.ui.components.CashManagerTopHeader
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestError
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryContainer
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.theme.ForestSurfaceVariant
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel
import com.example.utils.SemesterUtils

@Composable
fun AccountScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val presupuesto by viewModel.activePresupuesto.collectAsStateWithLifecycle()
    val metodosPago by viewModel.allMetodosPago.collectAsStateWithLifecycle()
    val categorias by viewModel.allCategorias.collectAsStateWithLifecycle()
    val notificaciones by viewModel.allNotificaciones.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user?.nombre ?: "") }
    var editUniv by remember { mutableStateOf(user?.universidad ?: "") }
    var editCiclo by remember { mutableStateOf(user?.ciclo ?: SemesterUtils.calculateSemester()) }
    var editCarrera by remember { mutableStateOf(user?.carrera ?: "") }
    var editAvatarUrl by remember { mutableStateOf(user?.avatarUrl ?: "") }
    var profileEditError by remember { mutableStateOf<String?>(null) }

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var newPaymentName by remember { mutableStateOf("") }
    var newPaymentType by remember { mutableStateOf("DIGITAL") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        CashManagerTopHeader(
            title = "Cash Manager",
            subtitle = "Gestión de Cuenta",
            avatarUrl = user?.avatarUrl,
            showBackButton = true,
            onBackClick = { viewModel.navigateBack() },
            onAvatarClick = { }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ForestSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().testTag("profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(72.dp)) {
                        if (!user?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ForestSurfaceVariant)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ForestPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.nombre?.take(1)?.uppercase() ?: "A",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = ForestOnPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user?.nombre ?: "Estudiante",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
                    Text(
                        text = user?.correo ?: "estudiante@universidad.edu",
                        style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSurfaceContainerLow)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = user?.universidad ?: "Universidad Nacional",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ForestSecondaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = user?.ciclo ?: "Semestre 2025-1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            editName = user?.nombre ?: ""
                            editUniv = user?.universidad ?: ""
                            editCiclo = user?.ciclo ?: SemesterUtils.calculateSemester()
                            editCarrera = user?.carrera ?: ""
                            editAvatarUrl = user?.avatarUrl ?: ""
                            profileEditError = null
                            showEditProfileDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSurfaceContainer),
                        shape = RoundedCornerShape(9999.dp),
                        modifier = Modifier.height(38.dp).testTag("btn_edit_profile")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Editar Perfil",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = ForestPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Presupuesto Config Section
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ForestSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Presupuesto inicial",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total inicial: ${CashManagerViewModel.formatCurrency(presupuesto?.metaTotal ?: 0.0)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestPrimary
                        )
                    )
                    Text(
                        text = "Gastable (80%): ${CashManagerViewModel.formatCurrency((presupuesto?.metaTotal ?: 0.0) * 0.8)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                    )
                    Text(
                        text = "Reserva protegida (20%): ${CashManagerViewModel.formatCurrency((presupuesto?.metaTotal ?: 0.0) * 0.2)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                    )
                }
            }

            // Payment Methods Management
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ForestSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Métodos de Pago",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                        }

                        IconButton(onClick = { showAddPaymentDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar método",
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    metodosPago.forEach { metodo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val mIcon = when (metodo.tipo) {
                                    "EFECTIVO" -> Icons.Default.Payments
                                    "DEBITO" -> Icons.Default.CreditCard
                                    else -> Icons.Default.AccountBalanceWallet
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ForestSurfaceContainerLow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = mIcon,
                                        contentDescription = null,
                                        tint = ForestPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = metodo.nombre,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = ForestOnSurface
                                        )
                                    )
                                    Text(
                                        text = "Saldo: ${CashManagerViewModel.formatCurrency(metodo.saldoActual)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                                    )
                                }
                            }

                            if (metodosPago.size > 1) {
                                IconButton(
                                    onClick = { viewModel.deletePaymentMethod(metodo.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = ForestError.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categories Management
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ForestSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Categorías",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                        }

                        IconButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar categoría",
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    categorias.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ForestPrimary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cat.nombre,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = ForestOnSurface
                                    )
                                )
                            }

                            if (categorias.size > 2) {
                                IconButton(
                                    onClick = { viewModel.deleteCategory(cat.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = ForestError.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notifications Center
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ForestSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Centro de Notificaciones",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                        }

                        if (notificaciones.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearNotifications() }) {
                                Text("Limpiar", color = ForestError, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (notificaciones.isEmpty()) {
                        Text(
                            text = "No tienes notificaciones pendientes.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant)
                        )
                    } else {
                        notificaciones.forEach { notif ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ForestSurfaceContainerLow)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = notif.titulo,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ForestPrimary
                                        )
                                    )
                                    Text(
                                        text = notif.mensaje,
                                        style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurface)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logout Button
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestSurfaceVariant),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_logout")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ForestPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = ForestPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Bottom Navigation Bar
        CashManagerBottomBar(
            currentScreen = AppScreen.ACCOUNT,
            onNavigate = { viewModel.navigateTo(it) }
        )
    }

    // Dialog: Edit Profile
    if (showEditProfileDialog) {
        val presetAvatars = listOf(
            "android.resource://com.example/drawable/profile_avatar_forest",
            "android.resource://com.example/drawable/profile_avatar_sage",
            "android.resource://com.example/drawable/profile_avatar_sky",
            "android.resource://com.example/drawable/profile_avatar_rose"
        )

        AlertDialog(
            onDismissRequest = {
                showEditProfileDialog = false
                profileEditError = null
            },
            title = {
                Text(
                    text = "Editar Perfil Universitario",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Foto de perfil",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurfaceVariant
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetAvatars.forEachIndexed { idx, url ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (editAvatarUrl == url) ForestPrimary else ForestSurfaceContainerLow)
                                    .padding(2.dp)
                                    .clickable {
                                        editAvatarUrl = url
                                        profileEditError = null
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Avatar $idx",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            editAvatarUrl = ""
                            profileEditError = null
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Quitar foto")
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            profileEditError = null
                        },
                        label = { Text("Nombre Completo") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = editName.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = editUniv,
                        onValueChange = {
                            editUniv = it
                            profileEditError = null
                        },
                        label = { Text("Universidad") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = editUniv.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("profile_university_input")
                    )
                    OutlinedTextField(
                        value = editCiclo,
                        onValueChange = {
                            editCiclo = it
                            profileEditError = null
                        },
                        label = { Text("Semestre / Ciclo") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCarrera,
                        onValueChange = {
                            editCarrera = it
                            profileEditError = null
                        },
                        label = { Text("Carrera / Programa") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    profileEditError?.let { error ->
                        Text(
                            text = error,
                            color = ForestError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            editName,
                            editUniv,
                            editCiclo,
                            editCarrera,
                            editAvatarUrl
                        ) { success, message ->
                            if (success) {
                                showEditProfileDialog = false
                                profileEditError = null
                            } else {
                                profileEditError = message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary)
                ) {
                    Text(
                        "Guardar",
                        color = ForestOnPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("save_profile_button")
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditProfileDialog = false
                    profileEditError = null
                }) {
                    Text("Cancelar", color = ForestOnSurfaceVariant)
                }
            }
        )
    }

    // Dialog: Add Payment Method
    if (showAddPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false },
            title = {
                Text(
                    text = "Nuevo Método de Pago",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPaymentName,
                        onValueChange = { newPaymentName = it },
                        label = { Text("Nombre (Ej. Nequi, Daviplata, Tarjeta)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addPaymentMethod(newPaymentName, newPaymentType)
                        newPaymentName = ""
                        showAddPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary)
                ) {
                    Text("Agregar", color = ForestOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaymentDialog = false }) {
                    Text("Cancelar", color = ForestOnSurfaceVariant)
                }
            }
        )
    }

    // Dialog: Add Category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    text = "Nueva Categoría",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nombre de la categoría") },
                        placeholder = { Text("Ej. Fotocopias, Salud, Libros") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCategory(newCategoryName)
                        newCategoryName = ""
                        showAddCategoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary)
                ) {
                    Text("Agregar", color = ForestOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancelar", color = ForestOnSurfaceVariant)
                }
            }
        )
    }

}
