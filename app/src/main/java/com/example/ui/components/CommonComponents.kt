package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import com.example.ui.viewmodel.AppScreen

@Composable
fun CashManagerLogoBadge(
    modifier: Modifier = Modifier,
    sizeDp: Int = 36
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape((sizeDp * 0.35f).dp))
            .background(ForestPrimary),
        contentAlignment = Alignment.Center
    ) {
        // Inner stylized mint & white rings
        Box(
            modifier = Modifier
                .size((sizeDp * 0.65f).dp)
                .clip(CircleShape)
                .background(ForestSecondaryFixed.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.35f).dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun CashManagerTopHeader(
    title: String = "Cash Manager",
    subtitle: String = "Inicio",
    avatarUrl: String? = null,
    onAvatarClick: () -> Unit = {},
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Surface(
        color = ForestBackground.copy(alpha = 0.95f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("back_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ForestOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                CashManagerLogoBadge(sizeDp = 32)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestPrimary,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ForestOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Student Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ForestSurfaceContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
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
                            text = "Estudiante",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestOnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Avatar Icon Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ForestPrimary)
                        .clickable(onClick = onAvatarClick)
                        .testTag("header_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar de usuario",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = ForestOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashManagerBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = ForestBackground.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Inicio / Home
            val isHome = currentScreen == AppScreen.HOME || currentScreen == AppScreen.HUB
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Inicio",
                isSelected = isHome,
                onClick = { onNavigate(AppScreen.HOME) },
                tag = "nav_tab_inicio"
            )

            // Tab 2: Horario / Agenda
            val isAgenda = currentScreen == AppScreen.AGENDA
            BottomNavItem(
                icon = Icons.Default.CalendarMonth,
                label = "Agenda",
                isSelected = isAgenda,
                onClick = { onNavigate(AppScreen.AGENDA) },
                tag = "nav_tab_agenda"
            )

            // Tab 3: Movimientos / Gastos
            val isGastos = currentScreen == AppScreen.TRANSACTIONS
            BottomNavItem(
                icon = Icons.Default.ReceiptLong,
                label = "Gastos",
                isSelected = isGastos,
                onClick = { onNavigate(AppScreen.TRANSACTIONS) },
                tag = "nav_tab_gastos"
            )

            // Tab 4: Perfil / Cuenta
            val isProfile = currentScreen == AppScreen.ACCOUNT
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Perfil",
                isSelected = isProfile,
                onClick = { onNavigate(AppScreen.ACCOUNT) },
                tag = "nav_tab_perfil"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) ForestPrimaryContainer else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ForestOnPrimary else ForestOnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ForestPrimary else ForestOnSurfaceVariant
            )
        )
    }
}
