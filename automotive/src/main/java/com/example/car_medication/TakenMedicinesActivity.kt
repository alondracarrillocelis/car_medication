package com.example.car_medication

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TakenMedicinesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taken_medicines)

        val takenList = DummyData.medicines.filter { it.taken }
        val container = findViewById<LinearLayout>(R.id.taken_medicines_container)
        val btnAddReminder = findViewById<Button>(R.id.btn_add_reminder)

        takenList.forEach {
            val tv = TextView(this)
            tv.text = "✅ ${it.name} - ${it.time}"
            tv.textSize = 20f
            container.addView(tv)
        }

        btnAddReminder.setOnClickListener {
            Toast.makeText(this, "¡Agrega un nuevo recordatorio divertido! 🎉", Toast.LENGTH_SHORT).show()
        }
    }
} 