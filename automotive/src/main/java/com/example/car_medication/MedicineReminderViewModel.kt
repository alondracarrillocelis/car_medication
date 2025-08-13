package com.example.car_medication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class MedicineReminderViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val remindersRef = db.collection("medicationReminders")

    fun addReminder(reminder: MedicineReminder, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Calcular la dosis por toma y horas automáticamente
                reminder.calculateSchedule()

                // Generar ID de documento
                val newDoc = remindersRef.document()
                reminder.id = newDoc.id
                reminder.createdAt = Date()

                // Guardar en Firestore
                newDoc.set(reminder).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("MedicineReminderVM", "Error al guardar recordatorio", e)
                onComplete(false)
            }
        }
    }

    fun updateReminder(reminder: MedicineReminder, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Recalcular horario y dosis por toma
                reminder.calculateSchedule()
                remindersRef.document(reminder.id).set(reminder).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("MedicineReminderVM", "Error al actualizar recordatorio", e)
                onComplete(false)
            }
        }
    }

    fun markAsTaken(reminder: MedicineReminder, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                remindersRef.document(reminder.id)
                    .update("completed", true)
                    .await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("MedicineReminderVM", "Error al marcar como tomado", e)
                onComplete(false)
            }
        }
    }

    fun snoozeReminder(reminder: MedicineReminder, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                remindersRef.document(reminder.id)
                    .update("snoozedAt", Date())
                    .await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("MedicineReminderVM", "Error al posponer recordatorio", e)
                onComplete(false)
            }
        }
    }

    fun getAllReminders(onResult: (List<MedicineReminder>) -> Unit) {
        remindersRef
            .orderBy("startDate")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MedicineReminderVM", "Error al obtener recordatorios", e)
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val reminders = snapshots?.mapNotNull { doc ->
                    doc.toObject(MedicineReminder::class.java).copy(id = doc.id)
                } ?: emptyList()
                onResult(reminders)
            }
    }
}
