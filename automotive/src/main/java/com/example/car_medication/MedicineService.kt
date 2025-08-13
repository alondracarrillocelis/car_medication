package com.example.car_medication

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.car_medication.MedicineReminder
import com.example.car_medication.AddMedicineReminderActivity
object MedicineService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addMedicineReminder(reminder: MedicineReminder) {
        reminder.calculateSchedule()

        firestore.collection("medicationReminders")
            .add(reminder)
            .await()
    }
}
