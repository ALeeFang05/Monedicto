package com.example.monedicto

import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class EncuestaAct : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encuesta)

        //Boton para enviar los resultados
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)

        btnCalcular.setOnClickListener {
            // 1. Verificamos que estén todas contestadas
            if (!preguntasNulas()) {
                Toast.makeText(this, "Por favor, responde todas las preguntas antes de enviar.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 2. Calculamos los puntos totales
            val totalPuntos = calcularPuntuacion()

            // 3. Evaluamos la puntuación según los rangos solicitados
            val diagnostico = when {
                totalPuntos in 0..1 -> "Sin problemas con el juego"
                totalPuntos in 2..5 -> "Potencial riesgo de adiccion"
                else -> "Problemas de riesgo adiccion al juego"
            }

            // 4. Mostramos el resultado junto con los puntos
            val mensaje = "Puntos: $totalPuntos\nDiagnóstico: $diagnostico"
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerRespuestas(): List<Pair<RadioGroup?, Int>> {
        return listOf(
            Pair(findViewById(R.id.rbG1), R.id.rb1_S),
            Pair(findViewById(R.id.rbGB2), R.id.rb2_S),
            Pair(findViewById(R.id.rbG3), R.id.rb3_S),
            Pair(findViewById(R.id.rbG4), R.id.rb4_S),
            Pair(findViewById(R.id.rbG5), R.id.rb5_S),
            Pair(findViewById(R.id.rbG6), R.id.rb6_S),
            Pair(findViewById(R.id.rbG7), R.id.rb7_S),
            Pair(findViewById(R.id.rbG8), R.id.rb8_S),
            Pair(findViewById(R.id.rbG9), R.id.rb9_S),
            Pair(findViewById(R.id.rbG10), R.id.rb10_S),
            Pair(findViewById(R.id.rbG11), R.id.rb11_S),
            Pair(findViewById(R.id.rbG12), R.id.rb12_S),
            Pair(findViewById(R.id.rbG13), R.id.rb13_S)
        )
    }

    // Función para validar que ningún grupo esté sin selección (-1 indica que no hay nada seleccionado)
    private fun preguntasNulas(): Boolean {
        for ((grupo, _) in obtenerRespuestas()) {
            if (grupo == null || grupo.checkedRadioButtonId == -1) {
                return false
            }
        }
        return true
    }

    // Función para sumar un punto por cada "Sí"
    private fun calcularPuntuacion(): Int {
        var totalPuntos = 0

        for ((grupo, idSi) in obtenerRespuestas()) {
            if (grupo?.checkedRadioButtonId == idSi) {
                totalPuntos++
            }
        }

        return totalPuntos
    }
}