package com.example.phone_medicatios.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.navigation.NavHostController
import com.example.phone_medicatios.R
import com.example.phone_medicatios.navigation.Screen
import com.example.phone_medicatios.viewmodel.ReminderViewModel
import com.example.phone_medicatios.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavHostController, viewModel: ReminderViewModel) {
    val purple = Color(0xFFB295C7)
    
    val medications by viewModel.medications.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Obtener fecha actual
    val currentDate = SimpleDateFormat("EEEE, dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
        .format(Date())

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(vertical = 40.dp)
        ) {
            // Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_medication),
                        contentDescription = "User Icon",
                        tint = purple,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bienvenido", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("¡Mantén tu salud al día! 💊", fontSize = 16.sp, color = Color.Gray)
                    }
                    
                    // Botón de historial
                    IconButton(
                        onClick = { navController.navigate(Screen.History.route) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            contentDescription = "Historial",
                            tint = purple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Fecha actual
                Text(
                    currentDate,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Estadísticas
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta de medicamentos
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = purple.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_medication),
                                contentDescription = null,
                                tint = purple,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${stats.totalMedications}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = purple
                            )
                            Text(
                                "Medicamentos",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Tarjeta de recordatorios
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${stats.activeReminders}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                            Text(
                                "Recordatorios",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de progreso del día
                Card(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = Color.Blue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Progreso del día",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                "${stats.completedToday} de ${stats.completedToday + stats.pendingToday} completadas",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "${if (stats.completedToday + stats.pendingToday > 0) (stats.completedToday * 100 / (stats.completedToday + stats.pendingToday)) else 0}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botones de acción
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón de crear recordatorio
                    Button(
                        onClick = { navController.navigate(Screen.ReminderForm.route) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = purple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo Recordatorio", fontSize = 14.sp)
                    }

                    // Botón de medicamentos
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Medications.route) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = purple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.phone_medicatios.R.drawable.ic_medication),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = purple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medicamentos", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Mensajes de estado
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                error,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            successMessage?.let { success ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                success,
                                color = Color.Green,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Horarios del día
            item {
                Text(
                    "Pendientes de hoy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            if (todaySchedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = purple.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = null,
                                tint = purple,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay horarios para hoy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Crea un recordatorio para ver tus horarios aquí",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(todaySchedules) { schedule ->
                    TodayScheduleCard(
                        schedule = schedule,
                        onMarkCompleted = { viewModel.markScheduleAsCompleted(schedule.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodayScheduleCard(
    schedule: TodaySchedule,
    onMarkCompleted: () -> Unit
) {
    val purple = Color(0xFFB295C7)
    val green = Color(0xFF4CAF50)
    val orange = Color(0xFFFF9800)

    // Mostrar solo si no está completado
    if (schedule.isCompleted) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isCompleted) green.copy(alpha = 0.05f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        border = if (schedule.isOverdue && !schedule.isCompleted) BorderStroke(1.dp, orange.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del medicamento
            Icon(
                painter = painterResource(id = R.drawable.ic_medication),
                contentDescription = null,
                tint = when {
                    schedule.isCompleted -> green
                    schedule.isOverdue -> orange
                    else -> purple
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            schedule.isCompleted -> green.copy(alpha = 0.1f)
                            schedule.isOverdue -> orange.copy(alpha = 0.1f)
                            else -> purple.copy(alpha = 0.1f)
                        }
                    )
                    .padding(12.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del horario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        schedule.medicationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.isCompleted) green else Color.Black
                    )
                    
                    if (schedule.isOverdue && !schedule.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = orange.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Atrasado",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = orange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    "${schedule.dosage} - ${schedule.time}",
                    fontSize = 14.sp,
                    color = if (schedule.isCompleted) green else Color.Gray
                )
                
                if (schedule.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "✓ Completado",
                        fontSize = 12.sp,
                        color = green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Botón de completar
            if (!schedule.isCompleted) {
                IconButton(
                    onClick = onMarkCompleted,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(green.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Marcar como completado",
                        tint = green,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completado",
                    tint = green,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(green.copy(alpha = 0.1f))
                        .padding(10.dp)
                )
            }
        }
    }
}
