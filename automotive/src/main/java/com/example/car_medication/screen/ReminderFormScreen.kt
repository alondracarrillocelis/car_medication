package com.example.phone_medicatios.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.phone_medicatios.R
import com.example.phone_medicatios.navigation.Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phone_medicatios.viewmodel.ReminderViewModel
import com.example.phone_medicatios.data.ReminderFormData
import com.example.phone_medicatios.components.StepItem
import androidx.navigation.NavBackStackEntry
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormScreen(
    navController: NavController,
    viewModel: ReminderViewModel,
    backStackEntry: NavBackStackEntry
) {
    val context = LocalContext.current
    val purple = Color(0xFFB295C7)
    
    val formData by viewModel.formData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Detectar si es edición (por parámetro en la ruta)
    val editMedId = backStackEntry.arguments?.getString("editMedId")
    val editReminderId = backStackEntry.arguments?.getString("editReminderId")
    val isEditReminderMode = !editReminderId.isNullOrBlank()
    val isEditMode = !editMedId.isNullOrBlank() || isEditReminderMode

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }

    // Mostrar mensaje de éxito y regresar tras guardar
    LaunchedEffect(successMessage) {
        if (isEditMode && successMessage?.contains("actualizado") == true) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    // Estados locales para los dropdowns
    var unidadExpanded by remember { mutableStateOf(false) }
    val unidadOptions = listOf("mg", "g", "mls")

    var tipoExpanded by remember { mutableStateOf(false) }
    val tipoOptions = listOf("Tableta", "Líquido", "Cápsula")

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
                            "Nuevo Recordatorio", 
                            fontSize = 26.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Configura tu medicamento", 
                            fontSize = 16.sp, 
                            color = Color.Gray
                        )
                    }
                }

                // Pasos del formulario centrado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepItem("1", "Elegir\nMedicamento", purple, true)
                    StepItem("2", "Configurar\nRecordatorio", purple, false)
                    StepItem("3", "Programa\nFinal", purple, false)
                }

                // Campos del formulario
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Campo Medicamento
                    OutlinedTextField(
                        value = formData.medication,
                        onValueChange = { 
                    viewModel.updateFormData(formData.copy(medication = it))
                        },
                        label = { 
                            Text(
                                "Nombre del medicamento", 
                                fontSize = 16.sp
                            ) 
                        },
                        placeholder = { 
                            Text(
                                "Ej: Paracetamol, Ibuprofeno...", 
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    // Campo Dosis
                    OutlinedTextField(
                        value = formData.dosage,
                        onValueChange = { 
                    viewModel.updateFormData(formData.copy(dosage = it))
                        },
                        label = { 
                            Text(
                                "Dosis", 
                                fontSize = 16.sp
                            ) 
                        },
                        placeholder = { 
                            Text(
                                "Ej: 500, 1000...", 
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                // Dropdown Unidad
                ExposedDropdownMenuBox(
                    expanded = unidadExpanded,
                    onExpandedChange = { unidadExpanded = !unidadExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.unit,
                        onValueChange = {},
                        readOnly = true,
                            label = { 
                                Text(
                                    "Unidad", 
                                    fontSize = 16.sp
                                ) 
                            },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purple,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                    )
                    ExposedDropdownMenu(
                        expanded = unidadExpanded,
                        onDismissRequest = { unidadExpanded = false }
                    ) {
                        unidadOptions.forEach { option ->
                            DropdownMenuItem(
                                    text = { 
                                        Text(
                                            option, 
                                            fontSize = 16.sp
                                        ) 
                                    },
                                onClick = {
                                    viewModel.updateFormData(formData.copy(unit = option))
                                    unidadExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown Tipo
                ExposedDropdownMenuBox(
                    expanded = tipoExpanded,
                    onExpandedChange = { tipoExpanded = !tipoExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.type,
                        onValueChange = {},
                        readOnly = true,
                            label = { 
                                Text(
                                    "Tipo de medicamento", 
                                    fontSize = 16.sp
                                ) 
                            },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purple,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                    )
                    ExposedDropdownMenu(
                        expanded = tipoExpanded,
                        onDismissRequest = { tipoExpanded = false }
                    ) {
                        tipoOptions.forEach { option ->
                            DropdownMenuItem(
                                    text = { 
                                        Text(
                                            option, 
                                            fontSize = 16.sp
                                        ) 
                                    },
                                onClick = {
                                    viewModel.updateFormData(formData.copy(type = option))
                                    tipoExpanded = false
                                }
                            )
                            }
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Cancelar", 
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = {
                            viewModel.clearMessages()
                            if (isEditReminderMode) {
                                viewModel.updateReminder(context, editReminderId!!, formData)
                            } else if (isEditMode) {
                                viewModel.updateMedication(editMedId!!, formData)
                            } else {
                            navController.navigate(Screen.ReminderSchedule.route)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = purple),
                        enabled = !isLoading && formData.medication.isNotBlank() && formData.dosage.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isEditReminderMode) "Guardar cambios" else if (isEditMode) "Guardar cambios" else "Siguiente", 
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
