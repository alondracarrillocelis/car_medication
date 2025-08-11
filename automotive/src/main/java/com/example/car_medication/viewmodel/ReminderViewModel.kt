package com.example.car_medication.viewmodel
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//data class ReminderFormData(
//    val medication: String = "",
//    val dosage: String = "",
//    val unit: String = "", // ✅ unidad añadida
//    val type: String = "",
//    val frequency: String = "",
//    val cycleWeeks: Int = 0,
//    val selectedDays: List<String> = emptyList(),
//    val firstDoseTime: String = "",
//    val doseTime: String = ""
//)
//
//class ReminderViewModel : ViewModel() {
//
//    private val db = FirebaseFirestore.getInstance()
//
//    private val _formData = MutableStateFlow(ReminderFormData())
//    val formData: StateFlow<ReminderFormData> = _formData
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage
//
//    private val _successMessage = MutableStateFlow<String?>(null)
//    val successMessage: StateFlow<String?> = _successMessage
//
//    private val _shouldNavigateToDashboard = MutableStateFlow(false)
//    val shouldNavigateToDashboard: StateFlow<Boolean> = _shouldNavigateToDashboard
//
//    fun updateFormData(data: ReminderFormData) {
//        _formData.value = data
//    }
//
//    fun clearMessages() {
//        _errorMessage.value = null
//        _successMessage.value = null
//    }
//
//    fun resetNavigation() {
//        _shouldNavigateToDashboard.value = false
//    }
//
//    fun saveReminder(context: Context) {
//        val data = formData.value
//
//        if (data.medication.isBlank() || data.firstDoseTime.isBlank()) {
//            _errorMessage.value = "Completa todos los campos necesarios"
//            return
//        }
//
//        _isLoading.value = true
//
//        val reminderMap = reminderToMap(data)
//
//        db.collection("reminders")
//            .add(reminderMap)
//            .addOnSuccessListener {
//                _isLoading.value = false
//                _successMessage.value = "Recordatorio guardado correctamente"
//                _shouldNavigateToDashboard.value = true
//            }
//            .addOnFailureListener { e ->
//                _isLoading.value = false
//                _errorMessage.value = "Error al guardar: ${e.message}"
//            }
//    }
//
//    fun updateReminder(context: Context, reminderId: String, data: ReminderFormData) {
//        if (data.medication.isBlank() || data.firstDoseTime.isBlank()) {
//            _errorMessage.value = "Completa todos los campos necesarios"
//            return
//        }
//
//        _isLoading.value = true
//
//        val reminderMap = reminderToMap(data)
//
//        db.collection("reminders").document(reminderId)
//            .update(reminderMap)
//            .addOnSuccessListener {
//                _isLoading.value = false
//                _successMessage.value = "Recordatorio actualizado correctamente"
//            }
//            .addOnFailureListener { e ->
//                _isLoading.value = false
//                _errorMessage.value = "Error al actualizar: ${e.message}"
//            }
//    }
//
//    // Puedes expandir esta función si tienes otros documentos de medicamento
//    fun updateMedication(medId: String, data: ReminderFormData) {
//        _errorMessage.value = "Función no implementada aún para medicamentos (ID: $medId)"
//    }
//
//    private fun reminderToMap(data: ReminderFormData): Map<String, Any> {
//        return mapOf(
//            "medication" to data.medication,
//            "dosage" to data.dosage,
//            "unit" to data.unit,
//            "type" to data.type,
//            "frequency" to data.frequency,
//            "cycleWeeks" to data.cycleWeeks,
//            "selectedDays" to data.selectedDays,
//            "firstDoseTime" to data.firstDoseTime,
//            "doseTime" to data.doseTime,
//            "timestamp" to System.currentTimeMillis()
//        )
//    }
//}
