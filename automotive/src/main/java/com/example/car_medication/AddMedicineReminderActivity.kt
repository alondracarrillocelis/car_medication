package com.example.car_medication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import java.util.Date

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
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val dosageValue = etDosage.text.toString().toDoubleOrNull() ?: 0.0
            val frequencyValue = etFrequency.text.toString().toIntOrNull() ?: 1

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
                userId = "user123" // TODO: reemplazar por el ID real del usuario logueado
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
