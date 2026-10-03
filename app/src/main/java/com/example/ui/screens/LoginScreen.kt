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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryContainer
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerHigh
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.theme.ForestSurfaceTint
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun LoginScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("estudiante@universidad.edu") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ForestBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Welcome Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ForestSurfaceContainerLow)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ForestOnPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "FINANZAS & ESTUDIO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ForestSurfaceTint
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ForestSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ciclo Activo 2025-I",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestOnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Bienvenido a Cash Manager",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gestiona tus finanzas y tu vida académica en un solo lugar.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ForestOnSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(ForestSurfaceContainerLowest.copy(alpha = 0.85f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Plataforma Oficial para Estudiantes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented Switcher Tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(ForestSurfaceContainerHigh)
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (!isRegisterMode) ForestPrimary else Color.Transparent)
                            .clickable { isRegisterMode = false }
                            .testTag("tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = if (!isRegisterMode) ForestOnPrimary else ForestOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Iniciar Sesión",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!isRegisterMode) ForestOnPrimary else ForestOnSurfaceVariant
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isRegisterMode) ForestPrimary else Color.Transparent)
                            .clickable { isRegisterMode = true }
                            .testTag("tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (isRegisterMode) ForestOnPrimary else ForestOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Registrarse",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isRegisterMode) ForestOnPrimary else ForestOnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ForestSurfaceContainerLowest)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (isRegisterMode) {
                        Column {
                            Text(
                                text = "Nombre Completo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestOnSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                placeholder = { Text("Ej. Valeria Montes") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = ForestOnSurfaceVariant)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(9999.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ForestSurfaceContainerLow,
                                    unfocusedContainerColor = ForestSurfaceContainerLow,
                                    focusedBorderColor = ForestPrimary,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_student_name")
                            )
                        }
                    }

                    // Email Field
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Correo institucional",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestOnSurface
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(ForestPrimaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = ForestSecondaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = ".edu admitido",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ForestSecondaryContainer,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("estudiante@universidad.edu") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ForestOnSurfaceVariant)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.Domain, contentDescription = null, tint = ForestSurfaceTint)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(9999.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ForestSurfaceContainerLow,
                                unfocusedContainerColor = ForestSurfaceContainerLow,
                                focusedBorderColor = ForestPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_campus_email")
                        )
                    }

                    // Password Field
                    Column {
                        Text(
                            text = "Contraseña",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ForestOnSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ForestOnSurfaceVariant)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Mostrar u ocultar contraseña",
                                        tint = ForestOnSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (isRegisterMode) {
                                    viewModel.register(nombre, email, password) { _, msg ->
                                        if (msg != null) viewModel.showToast(msg)
                                    }
                                } else {
                                    viewModel.login(email, password) { _, msg ->
                                        if (msg != null) viewModel.showToast(msg)
                                    }
                                }
                            }),
                            shape = RoundedCornerShape(9999.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ForestSurfaceContainerLow,
                                unfocusedContainerColor = ForestSurfaceContainerLow,
                                focusedBorderColor = ForestPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_password")
                        )
                    }

                    if (!isRegisterMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña? →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestSurfaceTint,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier
                                    .clickable { showForgotDialog = true }
                                    .padding(vertical = 4.dp)
                                    .testTag("forgot_password_link")
                            )
                        }
                    }

                    // Primary Submit Button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.register(nombre, email, password) { _, msg ->
                                    if (msg != null) viewModel.showToast(msg)
                                }
                            } else {
                                viewModel.login(email, password) { _, msg ->
                                    if (msg != null) viewModel.showToast(msg)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestPrimaryContainer,
                            contentColor = ForestOnPrimary
                        ),
                        shape = RoundedCornerShape(9999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_action_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isRegisterMode) "Crear Cuenta Universitaria" else "Iniciar Sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider "o continúa con"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ForestOnSurfaceVariant.copy(alpha = 0.2f))
                )
                Text(
                    text = "o continúa con",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForestOnSurfaceVariant
                    )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ForestOnSurfaceVariant.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Campus / Universitario Button
            Surface(
                onClick = { viewModel.quickGoogleLogin() },
                color = ForestSurfaceContainerLowest,
                shape = RoundedCornerShape(9999.dp),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_google_campus")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ForestSecondaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google Campus / Universitario",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestOnSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust badges (2 column grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ForestSurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ForestSecondaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cifrado Estudiantil",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                            Text(
                                text = "Datos 100% privados",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ForestOnSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ForestSurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ForestSurfaceTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ForestOnPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Presupuesto Ágil",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                            Text(
                                text = "Balance por semestre",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ForestOnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Terms notice
            Text(
                text = "Al continuar aceptas los términos de servicio y la política de privacidad universitaria.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ForestOnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Hemos enviado un enlace de restablecimiento seguro a tu correo institucional $email.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Entendido", color = ForestPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
