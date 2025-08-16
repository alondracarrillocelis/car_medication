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
    // Custom getter for endDate to handle potential String values from Firestore
    fun getSafeEndDate(): Date? {
        return when (endDate) {
            is Date -> endDate
            is String -> {
                try {
                    // Try to parse the string as a date
                    val dateText = endDate as String
                    val parts = dateText.split("/")
                    if (parts.size == 3) {
                        val day = parts[0].toInt()
                        val month = parts[1].toInt() - 1
                        val year = parts[2].toInt()
                        Calendar.getInstance().apply {
                            set(year, month, day)
                        }.time
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    // Custom getter for startDate to handle potential String values from Firestore
    fun getSafeStartDate(): Date? {
        return when (startDate) {
            is Date -> startDate
            is String -> {
                try {
                    // Try to parse the string as a date
                    val dateText = startDate as String
                    val parts = dateText.split("/")
                    if (parts.size == 3) {
                        val day = parts[0].toInt()
                        val month = parts[1].toInt() - 1
                        val year = parts[2].toInt()
                        Calendar.getInstance().apply {
                            set(year, month, day)
                        }.time
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    @Exclude
    fun isOngoing(): Boolean {
        val now = Date()
        return active && (getSafeEndDate() == null || now.before(getSafeEndDate()))
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

    companion object {
        /**
         * Safely converts a Firestore document to MedicineReminder, handling potential date conversion issues
         */
        fun fromFirestore(doc: com.google.firebase.firestore.DocumentSnapshot): MedicineReminder? {
            return try {
                doc.toObject(MedicineReminder::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                // Log the specific error for debugging
                android.util.Log.w("MedicineReminder", "Deserialization failed for doc ${doc.id}: ${e.message}")
                
                // If deserialization fails, try to manually construct the object
                try {
                    val data = doc.data
                    if (data != null) {
                        MedicineReminder(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            dosage = (data["dosage"] as? Number)?.toDouble() ?: 0.0,
                            dosagePerIntake = (data["dosagePerIntake"] as? Number)?.toDouble() ?: 0.0,
                            unit = data["unit"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            instructions = data["instructions"] as? String ?: "",
                            frequencyPerDay = (data["frequencyPerDay"] as? Number)?.toInt() ?: 1,
                            firstHour = data["firstHour"] as? String ?: "",
                            hoursList = (data["hoursList"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            days = (data["days"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            cycleWeeks = (data["cycleWeeks"] as? Number)?.toInt() ?: 0,
                            startDate = parseDateSafely(data["startDate"]),
                            endDate = parseDateSafely(data["endDate"]),
                            userId = data["userId"] as? String ?: "",
                            createdAt = parseDateSafely(data["createdAt"]) ?: Date(),
                            completed = data["completed"] as? Boolean ?: false,
                            active = data["active"] as? Boolean ?: true
                        )
                    } else {
                        android.util.Log.w("MedicineReminder", "Document ${doc.id} has no data")
                        null
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("MedicineReminder", "Manual construction failed for doc ${doc.id}: ${e2.message}")
                    null
                }
            }
        }

        private fun parseDateSafely(value: Any?): Date? {
            return when (value) {
                is Date -> value
                is String -> {
                    try {
                        val dateText = value.trim()
                        if (dateText.isEmpty()) return null
                        
                        // Try different date formats
                        val formats = listOf(
                            "dd/MM/yyyy",
                            "MM/dd/yyyy", 
                            "yyyy-MM-dd",
                            "dd-MM-yyyy"
                        )
                        
                        for (format in formats) {
                            try {
                                val parts = when (format) {
                                    "dd/MM/yyyy" -> dateText.split("/")
                                    "MM/dd/yyyy" -> dateText.split("/")
                                    "yyyy-MM-dd" -> dateText.split("-")
                                    "dd-MM-yyyy" -> dateText.split("-")
                                    else -> continue
                                }
                                
                                if (parts.size == 3) {
                                    val (first, second, third) = parts
                                    val (day, month, year) = when (format) {
                                        "dd/MM/yyyy" -> Triple(first.toInt(), second.toInt() - 1, third.toInt())
                                        "MM/dd/yyyy" -> Triple(second.toInt(), first.toInt() - 1, third.toInt())
                                        "yyyy-MM-dd" -> Triple(third.toInt(), second.toInt() - 1, first.toInt())
                                        "dd-MM-yyyy" -> Triple(first.toInt(), second.toInt() - 1, third.toInt())
                                        else -> continue
                                    }
                                    
                                    // Validate reasonable date ranges
                                    if (year in 1900..2100 && month in 0..11 && day in 1..31) {
                                        return Calendar.getInstance().apply {
                                            set(year, month, day)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.time
                                    }
                                }
                            } catch (e: Exception) {
                                // Continue to next format
                                continue
                            }
                        }
                        
                        // If no format worked, return null
                        null
                    } catch (e: Exception) {
                        android.util.Log.w("MedicineReminder", "Failed to parse date string: $value", e)
                        null
                    }
                }
                else -> null
            }
        }

        /**
         * Cleans up corrupted date fields in a Firestore document
         * Call this method when updating documents to fix any String dates
         */
        fun cleanupCorruptedDates(data: Map<String, Any?>): Map<String, Any?> {
            val cleanedData = data.toMutableMap()
            
            // Clean up startDate
            val startDate = parseDateSafely(data["startDate"])
            if (startDate != null) {
                cleanedData["startDate"] = startDate
            }
            
            // Clean up endDate
            val endDate = parseDateSafely(data["endDate"])
            if (endDate != null) {
                cleanedData["endDate"] = endDate
            }
            
            // Clean up createdAt
            val createdAt = parseDateSafely(data["createdAt"])
            if (createdAt != null) {
                cleanedData["createdAt"] = createdAt
            }
            
            return cleanedData
        }
    }
}
