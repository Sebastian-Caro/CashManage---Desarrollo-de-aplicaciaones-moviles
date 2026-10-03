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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.Recordatorio
import com.example.ui.components.CashManagerBottomBar
import com.example.ui.components.CashManagerTopHeader
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestError
import com.example.ui.theme.ForestOnPrimary
import com.example.ui.theme.ForestOnSecondaryContainer
import com.example.ui.theme.ForestOnSurface
import com.example.ui.theme.ForestOnSurfaceVariant
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryContainer
import com.example.ui.theme.ForestPrimaryFixedDim
import com.example.ui.theme.ForestSecondary
import com.example.ui.theme.ForestSecondaryContainer
import com.example.ui.theme.ForestSecondaryFixed
import com.example.ui.theme.ForestSurfaceContainer
import com.example.ui.theme.ForestSurfaceContainerHigh
import com.example.ui.theme.ForestSurfaceContainerLow
import com.example.ui.theme.ForestSurfaceContainerLowest
import com.example.ui.theme.ForestSurfaceVariant
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel

@Composable
fun AgendaScreen(
    viewModel: CashManagerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.activeUsuario.collectAsStateWithLifecycle()
    val recordatorios by viewModel.allRecordatorios.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val currentTab by viewModel.agendaTab.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDrawer by remember { mutableStateOf(false) }

    // Form states for new class
    var newClassName by remember { mutableStateOf("") }
    var newClassDay by remember { mutableStateOf(selectedDay) }
    var newClassAula by remember { mutableStateOf("") }
    var newClassStart by remember { mutableStateOf("08:00 AM") }
    var newClassEnd by remember { mutableStateOf("10:00 AM") }
    var newClassProf by remember { mutableStateOf("") }
    var newClassFacultad by remember { mutableStateOf("Ingeniería") }
    var newClassModalidad by remember { mutableStateOf("Teórica") }

    val scrollState = rememberScrollState()
    val daysList = listOf(
        Pair("Lun", "17"),
        Pair("Mar", "18"),
        Pair("Mié", "19"),
        Pair("Jue", "20"),
        Pair("Vie", "21"),
        Pair("Sáb", "22")
    )

    val dayNameMap = mapOf(
        "Lun" to "Lunes",
        "Mar" to "Martes",
        "Mié" to "Miércoles",
        "Jue" to "Jueves",
        "Vie" to "Viernes",
        "Sáb" to "Sábado"
    )

    // Filtered classes for current day
    val dayClasses = recordatorios.filter {
        (it.diaSemana.equals(selectedDay, ignoreCase = true) || it.diaSemana.startsWith(selectedDay.take(3), ignoreCase = true)) &&
        (searchQuery.isBlank() || it.titulo.contains(searchQuery, ignoreCase = true) || it.aula.contains(searchQuery, ignoreCase = true) || it.grupoOProfesor.contains(searchQuery, ignoreCase = true))
    }

    val heroClass = dayClasses.firstOrNull()
    val remainingClasses = if (dayClasses.size > 1) dayClasses.drop(1) else emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        CashManagerTopHeader(
            title = "Cash Manager",
            subtitle = "Agenda",
            avatarUrl = user?.avatarUrl,
            onAvatarClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(ForestSecondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = ForestOnSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Semestre 2025-1 • Activo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestOnSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agenda Universitaria",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = { /* Toggle filter */ },
                        shape = CircleShape,
                        color = ForestSurfaceContainerLowest,
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filtro",
                                tint = ForestPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Quick Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar clase, aula o profesor...", fontSize = 14.sp) },
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
                    .height(50.dp)
                    .testTag("input_search_classes")
            )

            // Segmented Tab Switcher [ Actividades ] [ Horario de clases ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(ForestSurfaceContainer)
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (currentTab == 0) ForestPrimaryContainer else Color.Transparent)
                            .clickable { viewModel.setAgendaTab(0) }
                            .testTag("tab_actividades"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (currentTab == 0) ForestOnPrimary else ForestOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Actividades",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentTab == 0) ForestOnPrimary else ForestOnSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ForestError)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (currentTab == 1) ForestPrimaryContainer else Color.Transparent)
                            .clickable { viewModel.setAgendaTab(1) }
                            .testTag("tab_horario"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarViewWeek,
                                contentDescription = null,
                                tint = if (currentTab == 1) ForestOnPrimary else ForestOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Horario de clases",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentTab == 1) ForestOnPrimary else ForestOnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Weekly Horizontal Day Selector Bar
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEMANA 8 • MARZO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ForestOnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Hoy es Martes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ForestPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysList.forEach { (shortDay, dayNum) ->
                        val fullName = dayNameMap[shortDay] ?: "Martes"
                        val isSelected = selectedDay.equals(fullName, ignoreCase = true) || selectedDay.startsWith(shortDay, ignoreCase = true)

                        Surface(
                            onClick = { viewModel.setSelectedDay(fullName) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ForestPrimaryContainer else ForestSurfaceContainerLowest,
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .width(50.dp)
                                .height(64.dp)
                                .testTag("day_selector_$shortDay")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = shortDay,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) ForestPrimaryFixedDim else ForestOnSurfaceVariant
                                    )
                                )
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ForestOnPrimary else ForestOnSurface
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ForestSecondaryFixed else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // Hero Card: Próxima clase destacada (One-Hero Rule)
            if (heroClass != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(ForestSurfaceVariant)
                        .padding(18.dp)
                        .testTag("hero_class_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(ForestPrimaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = ForestSecondaryFixed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Próxima clase en 35 min",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ForestOnPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { viewModel.showToast("Editar materia") },
                                    shape = CircleShape,
                                    color = ForestSurfaceContainerLowest,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = ForestPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Surface(
                                    onClick = { viewModel.showToast("Etiquetado rápido") },
                                    shape = CircleShape,
                                    color = ForestSurfaceContainerLowest,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Label,
                                            contentDescription = "Etiqueta",
                                            tint = ForestPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ForestSecondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = heroClass.facultad,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestOnSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = heroClass.grupoOProfesor,
                                style = MaterialTheme.typography.labelSmall.copy(color = ForestOnSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = heroClass.titulo,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${heroClass.horaInicio} - ${heroClass.horaFin}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestOnSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${heroClass.aula}",
                                style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(ForestSurfaceContainerLowest)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = ForestSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Notificación 15 min antes",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ForestOnSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.showToast("Ruta a Aula 304 (Piso 3)") }
                            ) {
                                Text(
                                    text = heroClass.piso,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = ForestPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = ForestPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Clases restantes de hoy
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clases restantes de hoy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestPrimary
                        )
                    )
                    Text(
                        text = "${remainingClasses.size} pendientes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ForestOnSurfaceVariant
                        )
                    )
                }

                if (remainingClasses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ForestSurfaceContainerLowest)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay más clases programadas para este día.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ForestOnSurfaceVariant)
                        )
                    }
                } else {
                    remainingClasses.forEach { clase ->
                        ClassItemCard(
                            clase = clase,
                            onDelete = { viewModel.deleteClass(clase.id) }
                        )
                    }
                }
            }

            // Action: + Agregar nueva materia/clase
            Button(
                onClick = { showAddDrawer = !showAddDrawer },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestPrimaryContainer,
                    contentColor = ForestOnPrimary
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_open_add_class")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Agregar nueva materia/clase",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Drawer / Form to Add New Class
            if (showAddDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(ForestSurfaceContainerLowest)
                        .padding(18.dp)
                        .testTag("new_class_drawer")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ForestSecondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = ForestOnSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nueva Materia",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestPrimary
                                    )
                                )
                            }

                            IconButton(
                                onClick = { showAddDrawer = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = ForestOnSurfaceVariant
                                )
                            }
                        }

                        // Form fields
                        OutlinedTextField(
                            value = newClassName,
                            onValueChange = { newClassName = it },
                            label = { Text("Nombre de la materia") },
                            placeholder = { Text("Ej. Algoritmos Avanzados") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newClassDay,
                                onValueChange = { newClassDay = it },
                                label = { Text("Día") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newClassAula,
                                onValueChange = { newClassAula = it },
                                label = { Text("Aula / Edificio") },
                                placeholder = { Text("Ej. Aula 102") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newClassStart,
                                onValueChange = { newClassStart = it },
                                label = { Text("Hora inicio") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newClassEnd,
                                onValueChange = { newClassEnd = it },
                                label = { Text("Hora fin") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = newClassProf,
                            onValueChange = { newClassProf = it },
                            label = { Text("Profesor / Grupo") },
                            placeholder = { Text("Ej. Prof. García") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showAddDrawer = false },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSurfaceVariant),
                                shape = RoundedCornerShape(9999.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Cancelar", color = ForestPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    viewModel.addClass(
                                        titulo = newClassName,
                                        dia = newClassDay,
                                        horaInicio = newClassStart,
                                        horaFin = newClassEnd,
                                        aula = newClassAula,
                                        profesor = newClassProf,
                                        facultad = newClassFacultad,
                                        modalidad = newClassModalidad,
                                        onSuccess = {
                                            newClassName = ""
                                            newClassAula = ""
                                            newClassProf = ""
                                            showAddDrawer = false
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestPrimaryContainer),
                                shape = RoundedCornerShape(9999.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Guardar materia", color = ForestOnPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Delighter banner: Healthy schedule tip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ForestSurfaceContainerLow)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ForestSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = ForestSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ventana libre de 2 horas",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestPrimary
                            )
                        )
                        Text(
                            text = "Entre clases tienes tiempo para almorzar e hidratarte en el campus.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        CashManagerBottomBar(
            currentScreen = AppScreen.AGENDA,
            onNavigate = { viewModel.navigateTo(it) }
        )
    }
}

@Composable
fun ClassItemCard(
    clase: Recordatorio,
    onDelete: () -> Unit
) {
    val icon = if (clase.facultad == "Sistemas") Icons.Default.Terminal else Icons.Default.AccountBalance

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ForestSurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ForestSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ForestPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = clase.facultad,
                                style = MaterialTheme.typography.labelSmall.copy(color = ForestOnSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(ForestSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = clase.modalidad,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ForestSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Text(
                            text = clase.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestOnSurface
                            )
                        )

                        Text(
                            text = "${clase.horaInicio} - ${clase.horaFin} • ${clase.aula}",
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestOnSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Mini Action Buttons: Edit and Delete
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = { /* edit */ },
                        shape = CircleShape,
                        color = ForestSurfaceContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = ForestPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = ForestError,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom row: notification active badge + teacher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ForestSurfaceContainerLow)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = ForestSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Notificación activa (15m antes)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Text(
                    text = clase.grupoOProfesor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForestOnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
