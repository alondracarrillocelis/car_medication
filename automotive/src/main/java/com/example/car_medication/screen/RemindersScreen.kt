package com.example.phone_medicatios.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.phone_medicatios.viewmodel.ReminderViewModel
import com.example.phone_medicatios.data.Reminder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(navController: NavController, viewModel: ReminderViewModel) {
    val purple = Color(0xFFB295C7)
    val reminders by viewModel.reminders.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header personalizado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
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
                Text(
                    "Mis Recordatorios",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
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
                            success,
                            color = Color.Green,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Lista de recordatorios
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (reminders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
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
                                    "No hay recordatorios",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crea tu primer recordatorio para verlos aquí",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onDelete = { viewModel.deleteReminder(reminder.id) },
                            onEdit = {
                                // Precargar datos en el ViewModel y navegar al formulario de edición
                                viewModel.updateFormData(
                                    viewModel.formData.value.copy(
                                        medication = reminder.medicationName,
                                        dosage = reminder.dosage,
                                        unit = reminder.unit,
                                        type = reminder.type,
                                        frequency = reminder.frequency,
                                        firstDoseTime = reminder.firstDoseTime,
                                        doseTime = reminder.doseTime
                                    )
                                )
                                navController.navigate(Screen.ReminderForm.route + "?editReminderId=" + reminder.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val purple = Color(0xFFB295C7)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del recordatorio
            Icon(
                painter = painterResource(id = R.drawable.ic_clock),
                contentDescription = null,
                tint = purple,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(purple.copy(alpha = 0.1f))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Información del recordatorio
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    reminder.medicationName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${reminder.dosage} ${reminder.unit} - ${reminder.type}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when (reminder.frequency) {
                        "Cíclicamente" -> "Frecuencia: ${reminder.frequency} (Cada ${reminder.cycleWeeks} semanas)"
                        else -> "Frecuencia: ${reminder.frequency}"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Primera dosis: ${reminder.firstDoseTime}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (reminder.doseTime.isNotBlank()) {
                    Text(
                        "Segunda dosis: ${reminder.doseTime}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Agregado: ${dateFormat.format(reminder.createdAt)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            // Botón de editar
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(purple.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = purple,
                    modifier = Modifier.size(20.dp)
                )
            }
            // Botón de eliminar
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
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
} 