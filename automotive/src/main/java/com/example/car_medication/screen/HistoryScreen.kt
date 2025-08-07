package com.example.phone_medicatios.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.phone_medicatios.R
import com.example.phone_medicatios.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phone_medicatios.viewmodel.ReminderViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: ReminderViewModel) {
    val purple = Color(0xFFB295C7)
    val reminders by viewModel.reminders.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }
    
    // Recargar datos cuando se entra a la pantalla
    LaunchedEffect(Unit) {
        Log.d("HistoryScreen", "Entrando a la pantalla de historial - recargando datos")
        viewModel.refreshData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header personalizado con contador
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = purple
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Historial de Recordatorios",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            "${reminders.size} recordatorio${if (reminders.size != 1) "s" else ""}",
                            fontSize = 14.sp,
                            color = purple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Botones de debug eliminados
                }
            }

            // Mensajes de estado
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "❌ $error",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (error.contains("índices")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Solución: Ejecuta el script setup_firebase.ps1 en PowerShell como administrador",
                                color = Color.Blue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            successMessage?.let { success ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = success,
                            color = Color.Green,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (reminders.isNotEmpty()) {
                // Ordenar recordatorios por fecha de creación (más recientes primero)
                val sortedReminders = reminders.sortedByDescending { it.createdAt }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedReminders) { reminder ->
                        var completedToday by remember { mutableStateOf(0) }
                        var totalToday by remember { mutableStateOf(0) }
                        
                        LaunchedEffect(reminder.id) {
                            try {
                                val progress = viewModel.getDailyProgressForReminder(reminder.id)
                                completedToday = progress.first
                                totalToday = progress.second
                            } catch (e: Exception) {
                                Log.e("HistoryScreen", "Error obteniendo progreso para reminder ${reminder.id}", e)
                                completedToday = 0
                                totalToday = 0
                            }
                        }
                        
                        ReminderHistoryItem(
                            reminder = reminder,
                            completedToday = completedToday,
                            totalToday = totalToday,
                            onDelete = { viewModel.deleteReminder(reminder.id) }
                        )
                    }
                }
            } else {
                // Estado vacío mejorado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = purple.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_medication),
                                    contentDescription = null,
                                    tint = purple,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "📋 Historial Vacío",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aún no has creado ningún recordatorio",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "💡 Los recordatorios que crees aparecerán aquí con toda su información",
                                    fontSize = 14.sp,
                                    color = purple,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { navController.navigate(Screen.ReminderForm.route) },
                                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_medication),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Crear Mi Primer Recordatorio",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderHistoryItem(
    reminder: com.example.phone_medicatios.data.Reminder,
    completedToday: Int,
    totalToday: Int,
    onDelete: () -> Unit
) {
    val purple = Color(0xFFB295C7)
    val green = Color(0xFF4CAF50)
    val orange = Color(0xFFFF9800)
    
    // Estado para expandir/colapsar
    var isExpanded by remember { mutableStateOf(false) }
    
    // Formatear fecha de creación
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    val createdDate = dateFormat.format(reminder.createdAt)
    
    // Progreso diario
    val dailyProgress = if (totalToday > 0) completedToday.toFloat() / totalToday else 0f

    // Progreso histórico (total)
    val progressPercentage = if (reminder.totalDoses > 0) {
        (reminder.completedDoses * 100) / reminder.totalDoses
    } else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header principal - siempre visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_medication),
                        contentDescription = null,
                        tint = purple,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            reminder.medicationName.ifBlank { "Medicamento" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            "${reminder.dosage} ${reminder.unit} - ${reminder.type}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            if (reminder.doseTime.isNotBlank()) {
                                "${reminder.firstDoseTime} - ${reminder.doseTime}"
                            } else {
                                "Hora: ${reminder.firstDoseTime}"
                            },
                            fontSize = 12.sp,
                            color = purple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Botones de acción
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón expandir/colapsar
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                            tint = purple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Botón eliminar
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Información detallada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🔄 Frecuencia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = purple
                            )
                            Text(
                                when (reminder.frequency) {
                                    "Cíclicamente" -> "${reminder.frequency} (Cada ${reminder.cycleWeeks} semanas)"
                                    else -> reminder.frequency
                                },
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "📅 Creado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = purple
                            )
                            Text(
                                createdDate,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progreso de dosis
                    Column {
                        Text(
                            "📊 Progreso de dosis (hoy)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = purple
                        )
                        Text(
                            "$completedToday/$totalToday dosis completadas hoy",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Barra de progreso diaria
                        if (totalToday > 0) {
                            LinearProgressIndicator(
                                progress = dailyProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = green,
                                trackColor = Color.Gray.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${(dailyProgress * 100).toInt()}% completado hoy",
                                fontSize = 12.sp,
                                color = green,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Estado activo/inactivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (reminder.isActive) green.copy(alpha = 0.1f) else orange.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (reminder.isActive) "✅ Activo" else "⏸️ Inactivo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (reminder.isActive) green else orange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 