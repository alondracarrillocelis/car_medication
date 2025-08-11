package com.example.car_medication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicationAdapter(
    private val medications: MutableList<MedicineReminder>,
    private val onMedicationClick: (MedicineReminder) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedicationName: TextView = itemView.findViewById(R.id.tv_medication_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvMedicationName.text = medication.name

        holder.itemView.setOnClickListener {
            onMedicationClick(medication)
        }
    }

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
