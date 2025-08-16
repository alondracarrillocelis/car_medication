package com.example.car_medication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMedicineReminderActivity : AppCompatActivity() {

    private val viewModel: MedicineReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_reminder)

        val etName = findViewById<EditText>(R.id.etName)
        val etDosage = findViewById<EditText>(R.id.etDosage)
        val etUnit = findViewById<EditText>(R.id.etUnit)
        val etType = findViewById<EditText>(R.id.etType)
        val etInstructions = findViewById<EditText>(R.id.etInstructions)
        val etFrequency = findViewById<EditText>(R.id.etFrequency)
        val etFirstHour = findViewById<EditText>(R.id.etFirstHour)
        val etEndDate = findViewById<EditText>(R.id.etEndDate) // <-- Debe existir en tu layout
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val dosageValue = etDosage.text.toString().toDoubleOrNull() ?: 0.0
            val frequencyValue = etFrequency.text.toString().toIntOrNull() ?: 1

            // Parsear la fecha de finalización
            val endDateString = etEndDate.text.toString().trim()
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate: Date? = try {
                formatter.parse(endDateString)
            } catch (e: ParseException) {
                null
            }

            if (parsedDate == null) {
                Toast.makeText(this, "Formato de fecha inválido (usa dd/MM/yyyy)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reminder = MedicineReminder(
                name = etName.text.toString().trim(),
                dosage = dosageValue,
                unit = etUnit.text.toString().trim(),
                type = etType.text.toString().trim(),
                instructions = etInstructions.text.toString().trim(),
                frequencyPerDay = frequencyValue,
                firstHour = etFirstHour.text.toString().trim(),
                days = listOf("Lunes", "Martes"), // TODO: reemplazar por selección en UI
                startDate = Date(),
                endDate = parsedDate, // Guardado como Date
                userId = "user123" // TODO: reemplazar por ID real del usuario
            )

            reminder.calculateSchedule()

            viewModel.addReminder(reminder) { success ->
                if (success) {
                    Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
