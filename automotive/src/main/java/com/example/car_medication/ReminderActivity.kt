package com.example.car_medication

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import android.widget.CheckBox
import android.util.Log
import android.widget.TextView
import android.view.View
import android.widget.LinearLayout

class ReminderActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var pendingAdapter: MedicationAdapter
    private lateinit var completedAdapter: MedicationAdapter
    private lateinit var tvEmptyState: LinearLayout
    private lateinit var tvPendingTitle: TextView
    private lateinit var tvCompletedTitle: TextView

    // ViewModel con lógica de guardado
    private val reminderViewModel: MedicineReminderViewModel by viewModels()

    // Medicamento seleccionado en la lista
    private var selectedMedication: MedicineReminder? = null

    // Timer para verificar medicamentos pendientes
    private var medicationTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // Inicializar adaptadores
        pendingAdapter = MedicationAdapter(
            mutableListOf(),
            onMedicationClick = { medication ->
                // Click normal: seleccionar medicamento
                selectedMedication = medication
                Toast.makeText(this, "Seleccionaste: ${medication.name}", Toast.LENGTH_SHORT).show()
            },
            onMedicationLongClick = { medication ->
                // Click largo: entrar en modo selección múltiple
                updateSelectionIndicator()
                if (pendingAdapter.isSelectionMode()) {
                    val selectedCount = pendingAdapter.getSelectedMedications().size
                    Toast.makeText(this, "$selectedCount medicamento(s) seleccionado(s)", Toast.LENGTH_SHORT).show()
                }
                true
            }
        )

        completedAdapter = MedicationAdapter(
            mutableListOf(),
            onMedicationClick = { medication ->
                // Click normal: seleccionar medicamento
                selectedMedication = medication
                Toast.makeText(this, "Seleccionaste: ${medication.name}", Toast.LENGTH_SHORT).show()
            },
            onMedicationLongClick = { medication ->
                // Click largo: entrar en modo selección múltiple
                updateSelectionIndicator()
                if (completedAdapter.isSelectionMode()) {
                    val selectedCount = completedAdapter.getSelectedMedications().size
                    Toast.makeText(this, "$selectedCount medicamentos seleccionado(s)", Toast.LENGTH_SHORT).show()
                }
                true
            }
        )

        val rvPendingMedications = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_pending_medications)
        val rvCompletedMedications = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_completed_medications)
        
        rvPendingMedications.layoutManager = LinearLayoutManager(this)
        rvCompletedMedications.layoutManager = LinearLayoutManager(this)
        
        rvPendingMedications.adapter = pendingAdapter
        rvCompletedMedications.adapter = completedAdapter

        // Inicializar vistas del estado vacío
        tvEmptyState = findViewById(R.id.tv_empty_state)
        tvPendingTitle = findViewById(R.id.tv_pending_title)
        tvCompletedTitle = findViewById(R.id.tv_completed_title)
        val tvSelectionIndicator = findViewById<TextView>(R.id.tv_selection_indicator)

        // Cargar datos en tiempo real
        listenForMedications()

        // Iniciar timer para verificar medicamentos pendientes
        startMedicationTimer()

        // Manejar intent de notificación
        handleNotificationIntent()

        // Botón para agregar recordatorio
        findViewById<android.widget.FrameLayout>(R.id.btn_add_reminder).setOnClickListener {
            showAddReminderDialog()
        }

        // Botón para marcar como tomado
        findViewById<android.widget.FrameLayout>(R.id.btn_mark_taken).setOnClickListener {
            if (selectedMedication != null) {
                showActionDialog("¿Marcar medicamento como tomado?") {
                    markMedicationTaken(selectedMedication!!)
                }
            } else {
                Toast.makeText(this, "Selecciona un medicamento primero", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para posponer
        findViewById<android.widget.FrameLayout>(R.id.btn_snooze).setOnClickListener {
            if (selectedMedication != null) {
                showActionDialog("¿Posponer recordatorio?") {
                    snoozeMedication(selectedMedication!!)
                }
            } else {
                Toast.makeText(this, "Selecciona un medicamento primero", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para limpiar selección
        findViewById<android.widget.FrameLayout>(R.id.btn_clear_selection).setOnClickListener {
            clearAllSelections()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        medicationTimer?.cancel()
        medicationTimer = null
    }

    private fun listenForMedications() {
        // First try to order by startDate, but fall back to createdAt if there are issues
        db.collection("medicationReminders")
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // If ordering by startDate fails, try ordering by createdAt instead
                    Log.w("ReminderActivity", "Failed to order by startDate: ${e.message}, trying createdAt")
                    listenForMedicationsFallback()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    try {
                        val medications = mutableListOf<MedicineReminder>()
                        
                        // Procesar cada documento de forma segura
                        for (doc in snapshots) {
                            val reminder = MedicineReminder.fromFirestore(doc)
                            if (reminder != null) {
                                medications.add(reminder)
                            }
                        }
                        
                        // Separar medicamentos por estado
                        val pendingMedications = medications.filter { it.completed == false }
                        val completedMedications = medications.filter { it.completed == true }
                        
                        pendingAdapter.updateMedications(pendingMedications)
                        completedAdapter.updateMedications(completedMedications)
                        
                        // Actualizar contadores y estado vacío
                        updateMedicationCounts(pendingMedications.size, completedMedications.size)
                        updateEmptyState(medications.isEmpty())
                        
                        // Clean up any corrupted date fields in the background
                        cleanupCorruptedDates(snapshots)
                        
                        // Show message if no medications were loaded
                        if (medications.isEmpty()) {
                            showError("No se encontraron medicamentos")
                        }
                    } catch (e: Exception) {
                        Log.e("ReminderActivity", "Error processing medications: ${e.message}", e)
                        showError("Error al cargar medicamentos: ${e.message ?: "Error desconocido"}")
                    }
                }
            }
    }

    private fun listenForMedicationsFallback() {
        db.collection("medicationReminders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ReminderActivity", "Firestore error (fallback): ${e.message}", e)
                    Toast.makeText(this, "Error: ${e.message ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    try {
                        val medications = mutableListOf<MedicineReminder>()
                        
                        // Procesar cada documento de forma segura
                        for (doc in snapshots) {
                            val reminder = MedicineReminder.fromFirestore(doc)
                            if (reminder != null) {
                                medications.add(reminder)
                            }
                        }
                        
                        // Separar medicamentos por estado
                        val pendingMedications = medications.filter { it.completed == false }
                        val completedMedications = medications.filter { it.completed == true }
                        
                        pendingAdapter.updateMedications(pendingMedications)
                        completedAdapter.updateMedications(completedMedications)
                        
                        // Actualizar contadores y estado vacío
                        updateMedicationCounts(pendingMedications.size, completedMedications.size)
                        updateEmptyState(medications.isEmpty())
                        
                        // Clean up any corrupted date fields in the background
                        cleanupCorruptedDates(snapshots)
                        
                        // Show message if no medications were loaded
                        if (medications.isEmpty()) {
                            showError("Error al cargar medicamentos: ${e?.message ?: "Error desconocido"}")
                        }
                    } catch (e: Exception) {
                        Log.e("ReminderActivity", "Error processing medications (fallback): ${e.message}", e)
                        showError("Error al cargar medicamentos: ${e.message ?: "Error desconocido"}")
                    }
                }
            }
    }

    private fun cleanupCorruptedDates(snapshots: com.google.firebase.firestore.QuerySnapshot) {
        // Show progress indicator
        runOnUiThread {
            showError("Limpiando datos corruptos...")
        }
        
        // Run cleanup in background to avoid blocking the UI
        Thread {
            var cleanedCount = 0
            var errorCount = 0
            
            for (doc in snapshots) {
                try {
                    val data = doc.data
                    if (data != null) {
                        // Check if any date fields are strings
                        val hasStringDates = data.any { (key, value) ->
                            (key == "startDate" || key == "endDate" || key == "createdAt") && value is String
                        }
                        
                        if (hasStringDates) {
                            val cleanedData = MedicineReminder.cleanupCorruptedDates(data)
                            doc.reference.update(cleanedData)
                                .addOnSuccessListener {
                                    cleanedCount++
                                    Log.d("ReminderActivity", "Cleaned up corrupted dates for document ${doc.id}")
                                }
                                .addOnFailureListener { e ->
                                    errorCount++
                                    Log.e("ReminderActivity", "Failed to clean up document ${doc.id}: ${e.message}")
                                }
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                    Log.e("ReminderActivity", "Error cleaning up document ${doc.id}: ${e.message}")
                }
            }
            
            // Show completion message
            runOnUiThread {
                if (cleanedCount > 0) {
                    showError("Limpieza completada: $cleanedCount documentos corregidos")
                }
                if (errorCount > 0) {
                    showError("Errores durante la limpieza: $errorCount documentos")
                }
            }
        }.start()
    }

    private fun removeCorruptedDocument(docId: String) {
        db.collection("medicationReminders").document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d("ReminderActivity", "Removed corrupted document: $docId")
            }
            .addOnFailureListener { e ->
                Log.e("ReminderActivity", "Failed to remove corrupted document $docId: ${e.message}")
            }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        runOnUiThread {
            if (isEmpty) {
                tvEmptyState.visibility = View.VISIBLE
                tvPendingTitle.visibility = View.GONE
                tvCompletedTitle.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                tvPendingTitle.visibility = View.VISIBLE
                tvCompletedTitle.visibility = View.VISIBLE
            }
        }
    }

    private fun updateMedicationCounts(pendingCount: Int, completedCount: Int) {
        runOnUiThread {
            // Actualizar títulos con contadores
            tvPendingTitle.text = "🕐 Pendientes de Tomar ($pendingCount)"
            tvCompletedTitle.text = "✅ Ya Tomados ($completedCount)"
            
            // Mostrar mensaje si no hay medicamentos pendientes
            if (pendingCount == 0) {
                tvPendingTitle.visibility = View.GONE
            } else {
                tvPendingTitle.visibility = View.VISIBLE
            }
            
            // Mostrar mensaje si no hay medicamentos completados
            if (completedCount == 0) {
                tvCompletedTitle.visibility = View.GONE
            } else {
                tvCompletedTitle.visibility = View.VISIBLE
            }
            
            // Mostrar resumen diario si hay medicamentos
            if (pendingCount > 0 || completedCount > 0) {
                showDailySummary(pendingCount, completedCount)
            }
        }
    }

    private fun showDailySummary(pendingCount: Int, completedCount: Int) {
        val totalCount = pendingCount + completedCount
        val completionRate = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
        
        val summaryMessage = when {
            completionRate == 100 -> "¡Excelente! Has tomado todos tus medicamentos hoy 🎉"
            completionRate >= 75 -> "¡Muy bien! Has completado el ${completionRate}% de tus medicamentos 👍"
            completionRate >= 50 -> "Vas bien, has completado ${completionRate}% de tus medicamentos 💪"
            else -> "Recuerda tomar tus medicamentos pendientes. Has completado ${completionRate}% ⏰"
        }
        
        // Mostrar resumen solo si hay medicamentos y no se ha mostrado recientemente
        if (totalCount > 0) {
            showError(summaryMessage)
        }
    }

    private fun clearAllSelections() {
        pendingAdapter.clearSelection()
        completedAdapter.clearSelection()
        selectedMedication = null
        updateSelectionIndicator()
        Toast.makeText(this, "✅ Selección limpiada", Toast.LENGTH_SHORT).show()
    }

    private fun updateSelectionIndicator() {
        val tvSelectionIndicator = findViewById<TextView>(R.id.tv_selection_indicator)
        val pendingSelected = pendingAdapter.getSelectedMedications().size
        val completedSelected = completedAdapter.getSelectedMedications().size
        val totalSelected = pendingSelected + completedSelected
        
        if (totalSelected > 0) {
            tvSelectionIndicator.text = "($totalSelected seleccionado${if (totalSelected > 1) "s" else ""})"
            tvSelectionIndicator.visibility = View.VISIBLE
        } else {
            tvSelectionIndicator.visibility = View.GONE
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etDosage = dialogView.findViewById<EditText>(R.id.etDosage) // dosis total diaria
        val etUnit = dialogView.findViewById<EditText>(R.id.etUnit)
        val etType = dialogView.findViewById<EditText>(R.id.etType)
        val etInstructions = dialogView.findViewById<EditText>(R.id.etInstructions)
        val etFrequency = dialogView.findViewById<EditText>(R.id.etFrequency) // veces al día
        val etFirstHour = dialogView.findViewById<EditText>(R.id.etFirstHour)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)

        // Set default end date to current date
        val currentDate = Calendar.getInstance()
        etEndDate.setText(String.format("%02d/%02d/%04d", 
            currentDate.get(Calendar.DAY_OF_MONTH),
            currentDate.get(Calendar.MONTH) + 1,
            currentDate.get(Calendar.YEAR)))

        val selectedDays = mutableListOf<String>()
        val checkBoxes: List<Pair<CheckBox, String>> = listOf(
            dialogView.findViewById<CheckBox>(R.id.cbMonday) to "Lunes",
            dialogView.findViewById<CheckBox>(R.id.cbTuesday) to "Martes",
            dialogView.findViewById<CheckBox>(R.id.cbWednesday) to "Miércoles",
            dialogView.findViewById<CheckBox>(R.id.cbThursday) to "Jueves",
            dialogView.findViewById<CheckBox>(R.id.cbFriday) to "Viernes",
            dialogView.findViewById<CheckBox>(R.id.cbSaturday) to "Sábado",
            dialogView.findViewById<CheckBox>(R.id.cbSunday) to "Domingo"
        )

        // Selector de hora
        etFirstHour.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            android.app.TimePickerDialog(this, { _, h, m ->
                etFirstHour.setText(String.format("%02d:%02d", h, m))
            }, hour, minute, true).show()
        }

        // Selector de fecha de fin
        etEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            android.app.DatePickerDialog(this, { _, y, m, d ->
                val selectedDate = Calendar.getInstance().apply {
                    set(y, m, d)
                }
                etEndDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar Recordatorio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                // Guardar días seleccionados
                selectedDays.clear()
                for ((checkBox, day) in checkBoxes) {
                    if (checkBox.isChecked) selectedDays.add(day)
                }

                // Validaciones paso a paso con mensajes claros
                val missingFields = mutableListOf<String>()
                
                if (etName.text.isNullOrBlank()) {
                    missingFields.add("Nombre del medicamento")
                }
                if (etDosage.text.isNullOrBlank()) {
                    missingFields.add("Dosis")
                }
                if (etUnit.text.isNullOrBlank()) {
                    missingFields.add("Unidad")
                }
                if (etFrequency.text.isNullOrBlank()) {
                    missingFields.add("Frecuencia")
                }
                if (etFirstHour.text.isNullOrBlank()) {
                    missingFields.add("Primera hora")
                }
                if (etEndDate.text.isNullOrBlank()) {
                    missingFields.add("Fecha de término")
                }
                
                // Si hay campos faltantes, mostrar mensaje y no continuar
                if (missingFields.isNotEmpty()) {
                    val message = "Completa estos campos obligatorios:\n• ${missingFields.joinToString("\n• ")}"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                
                // Verificar que al menos un día esté seleccionado
                if (selectedDays.isEmpty()) {
                    Toast.makeText(this, "Selecciona al menos un día de la semana", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // Validate end date is not in the past
                val endDateText = etEndDate.text.toString()
                try {
                    val parts = endDateText.split("/")
                    if (parts.size == 3) {
                        val day = parts[0].toInt()
                        val month = parts[1].toInt() - 1
                        val year = parts[2].toInt()
                        val selectedEndDate = Calendar.getInstance().apply {
                            set(year, month, day)
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }
                        val now = Calendar.getInstance()
                        if (selectedEndDate.before(now)) {
                            Toast.makeText(this, "La fecha de fin no puede estar en el pasado", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Crear el objeto reminder
                val reminder = MedicineReminder(
                    name = etName.text.toString(),
                    dosage = etDosage.text.toString().toDouble(), // total diario
                    unit = etUnit.text.toString(),
                    type = etType.text.toString(),
                    instructions = etInstructions.text.toString(),
                    frequencyPerDay = etFrequency.text.toString().toInt(),
                    firstHour = etFirstHour.text.toString(),
                    days = if (selectedDays.isEmpty()) listOf("Lunes") else selectedDays,
                    startDate = Date(),
                    endDate = if (etEndDate.text.isNullOrBlank()) null else {
                        try {
                            val dateText = etEndDate.text.toString()
                            val parts = dateText.split("/")
                            if (parts.size == 3) {
                                val day = parts[0].toInt()
                                val month = parts[1].toInt() - 1 // Calendar months are 0-based
                                val year = parts[2].toInt()
                                Calendar.getInstance().apply {
                                    set(year, month, day)
                                }.time
                            } else {
                                Date() // fallback to current date
                            }
                        } catch (e: Exception) {
                            Date() // fallback to current date
                        }
                    },
                    userId = "user123"
                )

                // Guardar usando el ViewModel
                reminderViewModel.addReminder(reminder) { success ->
                    if (success) {
                        Toast.makeText(this, "✅ Recordatorio guardado exitosamente", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "❌ Error al guardar el recordatorio", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showActionDialog(message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Sí") { _, _ -> action() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun markMedicationTaken(medication: MedicineReminder) {
        if (medication.id.isBlank()) {
            Toast.makeText(this, "No se encontró el ID del medicamento", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("medicationReminders")
            .document(medication.id)
            .update("completed", true)
            .addOnSuccessListener {
                Toast.makeText(this, "${medication.name} marcado como tomado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al marcar como tomado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun snoozeMedication(medication: MedicineReminder) {
        if (medication.id.isBlank()) {
            Toast.makeText(this, "No se encontró el ID del medicamento", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("medicationReminders")
            .document(medication.id)
            .update("snoozedAt", Date())
            .addOnSuccessListener {
                Toast.makeText(this, "${medication.name} pospuesto", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al posponer", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startMedicationTimer() {
        medicationTimer = Timer()
        medicationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkPendingMedications()
            }
        }, 0, 60000) // Verificar cada minuto
    }

    private fun checkPendingMedications() {
        val now = Calendar.getInstance()
        val currentTime = now.time
        
        // Solo verificar medicamentos activos y no completados
        db.collection("medicationReminders")
            .whereEqualTo("active", true)
            .whereEqualTo("completed", false)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val reminder = MedicineReminder.fromFirestore(doc)
                    if (reminder != null && shouldShowReminder(reminder, currentTime)) {
                        showNotification(reminder)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReminderActivity", "Error checking pending medications: ${e.message}", e)
            }
    }

    private fun shouldShowReminder(reminder: MedicineReminder, currentTime: Date): Boolean {
        // Verificar si es hora de tomar el medicamento
        if (reminder.startDate == null || reminder.endDate == null) return false
        
        // Si ya pasó la fecha de fin, no mostrar recordatorio
        if (currentTime.after(reminder.endDate)) return false
        
        // Verificar si es hora de tomar (basado en firstHour y frequencyPerDay)
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTimeInMinutes = hour * 60 + minute
        
        // Parsear la hora inicial
        val timeParts = reminder.firstHour.split(":")
        if (timeParts.size != 2) return false
        
        val startHour = timeParts[0].toInt()
        val startMinute = timeParts[1].toInt()
        val startTimeInMinutes = startHour * 60 + startMinute
        
        // Calcular si es hora de tomar (cada X horas según frequencyPerDay)
        val intervalMinutes = 24 * 60 / reminder.frequencyPerDay
        val timeSinceStart = currentTimeInMinutes - startTimeInMinutes
        
        // Verificar si es un momento válido para tomar
        if (timeSinceStart < 0) return false
        
        val timeSlot = timeSinceStart / intervalMinutes
        val expectedTime = startTimeInMinutes + (timeSlot * intervalMinutes)
        
        // Mostrar recordatorio si estamos dentro de una ventana de 15 minutos
        val timeDifference = Math.abs(currentTimeInMinutes - expectedTime)
        return timeDifference <= 15
    }

    private fun showNotification(reminder: MedicineReminder) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Recordatorios de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordar tomar medicamentos"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Crear intent para marcar como tomado
        val markTakenIntent = android.content.Intent(this, ReminderActivity::class.java).apply {
            action = "MARK_TAKEN"
            putExtra("medication_id", reminder.id)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val markTakenPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            reminder.id.hashCode().toInt(), 
            markTakenIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Crear notificación más detallada
        val notificationBuilder = NotificationCompat.Builder(this, "reminder_channel")
            .setSmallIcon(R.drawable.bell)
            .setContentTitle("🕐 Hora de tomar tu medicamento")
            .setContentText("${reminder.name} - ${reminder.dosage} ${reminder.unit}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Es hora de tomar ${reminder.name}\n\n" +
                        "Dosis: ${reminder.dosage} ${reminder.unit}\n" +
                        "Hora: ${reminder.firstHour}\n" +
                        "Frecuencia: ${reminder.frequencyPerDay} veces al día"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // Patrón de vibración
            .setLights(0xFF0000FF.toInt(), 3000, 3000) // Luz azul por 3 segundos
            .addAction(R.drawable.taken, "Marcar como Tomado", markTakenPendingIntent)

        // Mostrar notificación
        val notificationId = reminder.id.hashCode().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        // También mostrar un Toast para confirmación inmediata
        runOnUiThread {
            Toast.makeText(
                this, 
                "🕐 Hora de tomar: ${reminder.name}", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun handleNotificationIntent() {
        if (intent.action == "MARK_TAKEN") {
            val medicationId = intent.getStringExtra("medication_id")
            if (medicationId != null && medicationId.isNotBlank()) {
                val reminder = MedicineReminder(id = medicationId)
                markMedicationTaken(reminder)
            } else {
                Log.w("ReminderActivity", "No se pudo obtener el ID del medicamento de la notificación.")
            }
        }
    }
}
