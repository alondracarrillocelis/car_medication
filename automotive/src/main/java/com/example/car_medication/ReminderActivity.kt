package com.example.car_medication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import android.widget.CheckBox

class ReminderActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: MedicationAdapter

    // ViewModel con lógica de guardado
    private val reminderViewModel: MedicineReminderViewModel by viewModels()

    // Medicamento seleccionado en la lista
    private var selectedMedication: MedicineReminder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // Inicializar adaptador
        adapter = MedicationAdapter(
            mutableListOf(),
            onMedicationClick = { medication ->
                selectedMedication = medication
                Toast.makeText(this, "Seleccionaste: ${medication.name}", Toast.LENGTH_SHORT).show()
            }
        )

        val rvMedications = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_medications)
        rvMedications.layoutManager = LinearLayoutManager(this)
        rvMedications.adapter = adapter

        // Cargar datos en tiempo real
        listenForMedications()

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
    }

    private fun listenForMedications() {
        db.collection("medicationReminders")
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val medications = snapshots.mapNotNull { doc ->
                        doc.toObject(MedicineReminder::class.java).copy(id = doc.id)
                    }
                    adapter.updateMedications(medications)
                }
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

        AlertDialog.Builder(this)
            .setTitle("Agregar Recordatorio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                // Guardar días seleccionados
                selectedDays.clear()
                for ((checkBox, day) in checkBoxes) {
                    if (checkBox.isChecked) selectedDays.add(day)
                }

                // Validaciones
                when {
                    etName.text.isNullOrBlank() -> {
                        Toast.makeText(this, "Ingresa el nombre del medicamento", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    etDosage.text.isNullOrBlank() -> {
                        Toast.makeText(this, "Ingresa la dosis diaria total", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    etFrequency.text.isNullOrBlank() -> {
                        Toast.makeText(this, "Ingresa la frecuencia (veces al día)", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    etFirstHour.text.isNullOrBlank() -> {
                        Toast.makeText(this, "Selecciona la primera hora", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
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
                    userId = "user123"
                )

                // Guardar usando el ViewModel
                reminderViewModel.addReminder(reminder) { success ->
                    if (success) {
                        Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
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
}
