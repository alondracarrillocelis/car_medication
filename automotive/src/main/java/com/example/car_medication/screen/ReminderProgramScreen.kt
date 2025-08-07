package com.example.phone_medicatios.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import com.example.phone_medicatios.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phone_medicatios.viewmodel.ReminderViewModel
import com.example.phone_medicatios.navigation.Screen
import com.example.phone_medicatios.components.StepItem
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderProgramScreen(navController: NavController, viewModel: ReminderViewModel) {
    val context = LocalContext.current
    val purple = Color(0xFFB295C7)
    
    val formData by viewModel.formData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val shouldNavigateToDashboard by viewModel.shouldNavigateToDashboard.collectAsState()

    // Logging para debugging
    LaunchedEffect(formData) {
        android.util.Log.d("ReminderProgramScreen", "FormData actualizado:")
        android.util.Log.d("ReminderProgramScreen", "- Medicamento: '${formData.medication}'")
        android.util.Log.d("ReminderProgramScreen", "- Dosis: '${formData.dosage}'")
        android.util.Log.d("ReminderProgramScreen", "- Hora de toma: '${formData.firstDoseTime}'")
    }

    // Información desde el ViewModel
    val medicamento = formData.medication
    val dosis = formData.dosage
    val horarios = if (formData.doseTime.isNotBlank()) {
        listOf(
            formData.firstDoseTime to "1 ${formData.type.lowercase()}",
            formData.doseTime to "1 ${formData.type.lowercase()}"
        )
    } else {
        listOf(formData.firstDoseTime to "1 ${formData.type.lowercase()}")
    }
    val iconos = listOf(R.drawable.ic_sun, R.drawable.ic_moon)

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }

    // Navegación automática al dashboard después del guardado exitoso
    LaunchedEffect(shouldNavigateToDashboard) {
        if (shouldNavigateToDashboard) {
            delay(2000) // Esperar 2 segundos para que el usuario vea el mensaje de éxito
            viewModel.resetNavigation()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Encabezado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_medication),
                        contentDescription = "Icono",
                        tint = purple,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(purple.copy(alpha = 0.1f))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Confirmar Recordatorio", 
                            fontSize = 26.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Revisa y confirma la información", 
                            fontSize = 16.sp, 
                            color = Color.Gray
                        )
                    }
                }

                // Pasos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepItem("1", "Elegir\nMedicamento", purple, false)
                    StepItem("2", "Configurar\nRecordatorio", purple, false)
                    StepItem("3", "Programa\nFinal", purple, true)
                }

                // Nombre y dosis del medicamento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = purple.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_medication),
                        contentDescription = null,
                        tint = purple,
                        modifier = Modifier
                                .size(32.dp)
                            .clip(CircleShape)
                                .background(purple.copy(alpha = 0.1f))
                            .padding(6.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                            Text(
                                medicamento, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                dosis, 
                                fontSize = 14.sp, 
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Mensaje informativo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = purple.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_medication),
                            contentDescription = null,
                            tint = purple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Revisa la información y confirma para guardar el recordatorio",
                            color = purple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Horarios configurados
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, purple.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "📅 Horarios Programados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = purple
                    )
                    
                    // Información de frecuencia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            contentDescription = null,
                            tint = purple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (formData.frequency) {
                                "Cíclicamente" -> "Frecuencia: ${formData.frequency} (Cada ${formData.cycleWeeks} semanas)"
                                "Días Seleccionados" -> "Frecuencia: ${formData.frequency} (${formData.selectedDays.joinToString(", ")})"
                                else -> "Frecuencia: ${formData.frequency}"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    horarios.forEachIndexed { index, (hora, cantidad) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = iconos.getOrElse(index) { R.drawable.ic_clock }),
                                    contentDescription = null,
                                    tint = purple,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    hora, 
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_pill),
                                    contentDescription = null,
                                    tint = purple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    cantidad, 
                                    fontSize = 16.sp, 
                                    color = purple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        if (index < horarios.size - 1) {
                            Divider(
                                color = purple.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Mensajes de estado
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "❌ $error",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                successMessage?.let { success ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { 
                            viewModel.clearMessages()
                            navController.popBackStack() 
                        },
                        border = BorderStroke(2.dp, purple),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = purple),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(
                            "Atrás", 
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.clearMessages()
                            viewModel.saveReminder(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = purple),
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Guardando...", 
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                "Guardar", 
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
