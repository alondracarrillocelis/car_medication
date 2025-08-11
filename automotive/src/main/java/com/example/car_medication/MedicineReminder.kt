package com.example.car_medication


data class MedicineReminder(
    val name: String,
    val time: String,
    val taken: Boolean = false
)
