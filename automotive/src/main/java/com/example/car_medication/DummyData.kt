package com.example.car_medication

data class MedicineReminder(
    val name: String,
    val time: String,
    val taken: Boolean = false
)

object DummyData {
    val medicines = listOf(
        MedicineReminder("Paracetamol 500mg", "08:00 AM"),
        MedicineReminder("Ibuprofeno 400mg", "12:00 PM"),
        MedicineReminder("Vitamina C", "06:00 PM"),
        MedicineReminder("Omeprazol 20mg", "09:00 PM")
    )
} 