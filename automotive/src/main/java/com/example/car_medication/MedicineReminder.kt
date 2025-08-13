package com.example.car_medication

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Calendar
import java.util.Date


@IgnoreExtraProperties
data class MedicineReminder(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var dosage: Double = 0.0,             // Cantidad total por día
    var dosagePerIntake: Double = 0.0,    // Cantidad por toma
    var unit: String = "",
    var type: String = "",
    var description: String = "",
    var instructions: String = "",
    var frequencyPerDay: Int = 1,
    var firstHour: String = "",
    var hoursList: List<String> = emptyList(),
    var days: List<String> = emptyList(),
    var cycleWeeks: Int = 0,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var userId: String = "",
    var createdAt: Date = Date(),
    var completed: Boolean = false,
    var active: Boolean = true
) {
    @Exclude
    fun isOngoing(): Boolean {
        val now = Date()
        return active && (endDate == null || now.before(endDate))
    }

    @Exclude
    fun calculateSchedule() {
        if (frequencyPerDay <= 0) return

        val dosisPorToma = dosage / frequencyPerDay
        val horas = mutableListOf<String>()

        val parts = firstHour.split(":")
        if (parts.size != 2) return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
        }

        val intervaloHoras = 24 / frequencyPerDay

        for (i in 0 until frequencyPerDay) {
            horas.add(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
            calendar.add(Calendar.HOUR_OF_DAY, intervaloHoras)
        }

        this.hoursList = horas
        this.dosagePerIntake = dosisPorToma
    }
}
