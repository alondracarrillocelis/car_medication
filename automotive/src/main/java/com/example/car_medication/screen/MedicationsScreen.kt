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
import com.example.phone_medicatios.data.Medication
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(navController: NavController, viewModel: ReminderViewModel) {
    val purple = Color(0xFFB295C7)
    
    val medications by viewModel.medications.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Forzar recarga de medicamentos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadMedications()
    }

    // Auto-ocultar mensajes de éxito después de unos segundos
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            viewModel.clearMessages()
        }
    }

    // Estado para mostrar el diálogo de nuevo medicamento
    var showAddDialog by remember { mutableStateOf(false) }

    // Estado de los campos del formulario
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medUnit by remember { mutableStateOf("mg") }
    var medType by remember { mutableStateOf("Tableta") }

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
                    "Mis Medicamentos",
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

            // Lista de medicamentos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (medications.isEmpty()) {
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
                                    painter = painterResource(id = R.drawable.ic_medication),
                                    contentDescription = null,
                                    tint = purple,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No hay medicamentos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crea tu primer recordatorio para ver tus medicamentos aquí",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(medications) { medication ->
                        MedicationCard(
                            medication = medication,
                            onDelete = { viewModel.deleteMedication(medication.id) },
                            onEdit = {
                                // Precargar datos en el ViewModel y navegar al formulario de edición
                                viewModel.updateFormData(
                                    viewModel.formData.value.copy(
                                        medication = medication.name,
                                        dosage = medication.dosage,
                                        unit = medication.unit,
                                        type = medication.type,
                                        description = medication.description,
                                        instructions = medication.instructions
                                    )
                                )
                                navController.navigate(Screen.ReminderForm.route + "?editMedId=" + medication.id)
                            }
                        )
                    }
                }
            }
        }
        // FAB para añadir medicamento
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = purple,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir medicamento",
                tint = Color.White
            )
        }
        // Diálogo para añadir medicamento
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Medicamento") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("Nombre") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = medDosage,
                            onValueChange = { medDosage = it },
                            label = { Text("Dosis") },
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medUnit,
                                onValueChange = { medUnit = it },
                                label = { Text("Unidad") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = medType,
                                onValueChange = { medType = it },
                                label = { Text("Tipo") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (medName.isNotBlank() && medDosage.isNotBlank() && medUnit.isNotBlank() && medType.isNotBlank()) {
                                viewModel.addMedication(medName, medDosage, medUnit, medType)
                                showAddDialog = false
                                medName = ""
                                medDosage = ""
                                medUnit = "mg"
                                medType = "Tableta"
                            }
                        }
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val purple = Color(0xFFB295C7)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header principal - siempre visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pill_filled),
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
                        medication.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        "${medication.dosage} ${medication.unit} - ${medication.type}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                // Botón expandir/colapsar
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = purple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // Detalles expandibles
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (medication.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            medication.description,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Agregado: ${dateFormat.format(medication.createdAt)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
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
                        // Botón de editar eliminado temporalmente
                    }
                }
            }
        }
    }
} 