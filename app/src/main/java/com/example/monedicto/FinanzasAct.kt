package com.example.monedicto

import android.os.Bundle import android.os.PersistableBundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class FinanzasAct : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas)

        val editConcepto = findViewById<TextInputEditText>(R.id.editConcepto)
        val editMonto = findViewById<TextInputEditText>(R.id.editMonto)
        val btnSave = findViewById<Button>(R.id.btnGuardarAhorro)

        btnSave.setOnClickListener {
            val concepto = editConcepto.text.toString()
            val monto = editMonto.text.toString().toDoubleOrNull() ?: 0.0

            if (concepto.isEmpty() || monto <= 0){
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Guardando: $concepto por $ $monto", Toast.LENGTH_SHORT).show()
                //Regresa a la pantalla anterior
                finish()
            }

        }

    }
}