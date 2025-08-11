package com.example.car_medication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        db = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.rv_medications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        medicationAdapter = MedicationAdapter(mutableListOf()) { medication ->
            Toast.makeText(this, "Medicamento seleccionado: ${medication.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = medicationAdapter

        setupActionButtons()
        loadMedicationsFromFirebase()
    }

    private fun setupActionButtons() {
        val btnMarkTaken = findViewById<android.view.View>(R.id.btn_mark_taken)
        val btnSnooze = findViewById<android.view.View>(R.id.btn_snooze)
        val btnAddReminder = findViewById<android.view.View>(R.id.btn_add_reminder)

        btnMarkTaken.setOnClickListener {
            Toast.makeText(this, "¡Medicamento marcado como tomado! ✅", Toast.LENGTH_SHORT).show()
        }

        btnSnooze.setOnClickListener {
            Toast.makeText(this, "¡Hora de una siesta! 😴 Pospuesto.", Toast.LENGTH_SHORT).show()
        }

        btnAddReminder.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val etMedicationName = dialogView.findViewById<EditText>(R.id.et_medication_name)
        val etMedicationTime = dialogView.findViewById<EditText>(R.id.et_medication_time)

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        etMedicationTime.setText(currentTime)

        AlertDialog.Builder(this)
            .setTitle("Agregar Nuevo Recordatorio")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val name = etMedicationName.text.toString().trim()
                val time = etMedicationTime.text.toString().trim()

                if (name.isNotEmpty() && time.isNotEmpty()) {
                    addReminderToFirebase(name, time)
                } else {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addReminderToFirebase(name: String, time: String) {
        val reminder = hashMapOf(
            "name" to name,
            "time" to time,
            "taken" to false,
            "createdAt" to Date(),
            "userId" to "user123"
        )

        db.collection("medications")
            .add(reminder)
            .addOnSuccessListener {
                Toast.makeText(this, "Recordatorio agregado exitosamente! ✅", Toast.LENGTH_SHORT).show()
                loadMedicationsFromFirebase()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar recordatorio: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadMedicationsFromFirebase() {
        db.collection("medications")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val medications = mutableListOf<MedicineReminder>()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val time = document.getString("time") ?: ""
                    val taken = document.getBoolean("taken") ?: false

                    medications.add(MedicineReminder(name, time, taken))
                }
                medicationAdapter.updateMedications(medications)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error cargando medicamentos: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
