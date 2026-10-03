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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.CashManagerBottomBar
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSecondary
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerHigh
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.theme.ForestSurfaceVariant
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun DashboardHubScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val firstName = user?.nombre?.split(" ")?.firstOrNull() ?: "Alejandro"
    val ciclo = user?.ciclo ?: "Semestre 2025-1"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ambient Greeting & User Presence Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(ForestSurfaceContainerLow)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Semester badge
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
                                    text = ciclo,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestOnSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "¡Hola, $firstName!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "¿Qué deseas gestionar hoy?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ForestOnSurfaceVariant
                            )
                        )
                    }

                    // Avatar with verified dot
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { viewModel.navigateTo(AppScreen.ACCOUNT) }
                    ) {
                        if (!user?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ForestSurfaceVariant)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ForestPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = firstName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = ForestOnPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Bottom-right green check dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(ForestSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ForestOnSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            // Main Navigation Modules (Tactile Action Hub)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Module 1: Gastos
                HubModuleCard(
                    icon = Icons.Default.ReceiptLong,
                    title = "Gastos",
                    subtitle = "Registrar y monitorear compras",
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    testTag = "hub_card_gastos"
                )

                // Module 2: Presupuesto
                HubModuleCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Presupuesto",
                    subtitle = "Límites mensuales por categoría",
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    testTag = "hub_card_presupuesto"
                )

                // Module 3: Recordatorios y Horario
                HubModuleCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Recordatorios y Horario",
                    subtitle = "Clases semanales, parciales y entregas",
                    badge = "2 hoy",
                    onClick = { viewModel.navigateTo(AppScreen.AGENDA) },
                    testTag = "hub_card_horario"
                )

                // Module 4: Perfil
                HubModuleCard(
                    icon = Icons.Default.School,
                    title = "Perfil",
                    subtitle = "Universidad, preferencias y seguridad",
                    onClick = { viewModel.navigateTo(AppScreen.ACCOUNT) },
                    testTag = "hub_card_perfil"
                )
            }

            // Secondary Context Card / Study Companion Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ForestSurfaceContainer.copy(alpha = 0.7f))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = ForestPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Mantén el ritmo: tienes una entrega de Economía Aplicada a las 18:00 hrs.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ForestOnSurfaceVariant,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sign out button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = { viewModel.logout() },
                    shape = RoundedCornerShape(9999.dp),
                    color = ForestSurfaceVariant,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("btn_logout_hub")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = ForestPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar sesión",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary
                            )
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        CashManagerBottomBar(
            currentScreen = AppScreen.HUB,
            onNavigate = { viewModel.navigateTo(it) }
        )
    }
}

@Composable
private fun HubModuleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = ForestSurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ForestSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ForestPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface
                            )
                        )
                        if (badge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(ForestSecondaryFixed)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ForestOnSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ForestSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ForestOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
