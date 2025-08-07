package com.example.car_medication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.car_medication.DummyData

class ReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        val medicine = DummyData.medicines.firstOrNull { !it.taken } ?: DummyData.medicines.first()

        val tvName = findViewById<TextView>(R.id.tv_medicine_name)
        val tvTime = findViewById<TextView>(R.id.tv_medicine_time)
        val btnRemindLater = findViewById<Button>(R.id.btn_remind_later)
        val btnSnooze = findViewById<Button>(R.id.btn_snooze)

        tvName.text = medicine.name
        tvTime.text = medicine.time

        btnRemindLater.setOnClickListener {
            Toast.makeText(this, "¡Te recordaremos en unos minutos! ⏰", Toast.LENGTH_SHORT).show()
        }
        btnSnooze.setOnClickListener {
            Toast.makeText(this, "¡Hora de una siesta! 😴 Pospuesto.", Toast.LENGTH_SHORT).show()
        }
    }
} 