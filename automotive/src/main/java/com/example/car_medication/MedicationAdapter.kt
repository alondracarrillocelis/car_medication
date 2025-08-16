package com.example.car_medication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.car_medication.MedicineReminder
import java.text.SimpleDateFormat
import java.util.Locale

class MedicationAdapter(
    private val medications: MutableList<MedicineReminder>,
    private val onMedicationClick: (MedicineReminder) -> Unit,
    private val onMedicationLongClick: (MedicineReminder) -> Boolean = { false }
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    private val selectedMedications = mutableSetOf<String>()

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedicationName: TextView = itemView.findViewById(R.id.tv_med_name)
        val tvDosage: TextView = itemView.findViewById(R.id.tv_med_dose)
        val tvUnit: TextView = itemView.findViewById(R.id.tv_med_unit)
        val tvSchedule: TextView = itemView.findViewById(R.id.tv_med_time)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_med_status)
        val tvDays: TextView = itemView.findViewById(R.id.tv_med_days)
        val rootView: View = itemView
        val selectionIndicator: View = itemView.findViewById(R.id.selection_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        val isSelected = selectedMedications.contains(medication.id)

        // Aplicar estado de selección visual
        updateSelectionState(holder.rootView, isSelected)
        updateSelectionIndicator(holder, isSelected)

        // Nombre del medicamento
        holder.tvMedicationName.text = medication.name

        // Dosis por toma
        val dosageText = if (medication.dosagePerIntake > 0) {
            "Dosis: ${medication.dosagePerIntake} ${medication.unit}"
        } else {
            "Dosis: ${medication.dosage} ${medication.unit}"
        }
        holder.tvDosage.text = dosageText

        // Frecuencia
        val frequencyText = when (medication.frequencyPerDay) {
            1 -> "1 vez al día"
            else -> "${medication.frequencyPerDay} veces al día"
        }
        holder.tvUnit.text = frequencyText

        // Horario
        if (medication.hoursList.isNotEmpty()) {
            holder.tvSchedule.text = "Hora: ${medication.hoursList.joinToString(", ")}"
        } else {
            // Si no hay horario calculado, usar la hora inicial
            holder.tvSchedule.text = "Hora: ${medication.firstHour}"
        }

        // Estado del medicamento
        val statusText = when {
            medication.completed -> "Completado"
            !medication.active -> "Inactivo"
            else -> "Activo"
        }
        holder.tvStatus.text = statusText

        // Días de la semana
        if (medication.days.isNotEmpty()) {
            holder.tvDays.text = medication.days.joinToString(", ")
        } else {
            holder.tvDays.text = "Todos los días"
        }

        // Click normal
        holder.itemView.setOnClickListener {
            onMedicationClick(medication)
        }

        // Click largo para seleccionar
        holder.itemView.setOnLongClickListener {
            toggleSelection(medication.id)
            notifyItemChanged(position)
            onMedicationLongClick(medication)
        }
    }

    private fun updateSelectionState(itemView: View, isSelected: Boolean) {
        if (isSelected) {
            itemView.alpha = 0.8f
            itemView.elevation = 8f
            itemView.setBackgroundResource(R.drawable.selected_medication_shape)
        } else {
            itemView.alpha = 1.0f
            itemView.elevation = 2f
            itemView.setBackgroundResource(R.drawable.form_shape)
        }
    }

    private fun updateSelectionIndicator(holder: MedicationViewHolder, isSelected: Boolean) {
        holder.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    private fun toggleSelection(medicationId: String) {
        if (selectedMedications.contains(medicationId)) {
            selectedMedications.remove(medicationId)
        } else {
            selectedMedications.add(medicationId)
        }
    }

    fun getSelectedMedications(): List<MedicineReminder> {
        return medications.filter { selectedMedications.contains(it.id) }
    }

    fun clearSelection() {
        selectedMedications.clear()
        notifyDataSetChanged()
    }

    fun isSelectionMode(): Boolean = selectedMedications.isNotEmpty()

    override fun getItemCount(): Int = medications.size

    fun addMedicine(medicine: MedicineReminder) {
        medications.add(0, medicine) // Lo añade al inicio
        notifyItemInserted(0)
    }

    fun updateMedications(newMedications: Collection<MedicineReminder>) {
        medications.clear()
        medications.addAll(newMedications)
        notifyDataSetChanged()
    }
}
