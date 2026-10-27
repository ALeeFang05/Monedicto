package com.example.monedicto

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EncuestaAct : AppCompatActivity() {

    private lateinit var editComentarios: TextInputEditText
    private lateinit var btnCalcular: Button

    // ⚠️ CONFIGURACIÓN DE RETROFIT: Apunta al localhost de tu computadora
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://172.16.132.20:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MonedictoApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encuesta)

        // Conectar la casilla de comentarios del XML (Asegúrate de que este ID coincida con tu layout)
        editComentarios = findViewById(R.id.editComentarios)
        btnCalcular = findViewById(R.id.btnCalcular)

        btnCalcular.setOnClickListener {
            // 1. Verificamos que estén todas contestadas
            if (!preguntasNulas()) {
                Toast.makeText(this, "Por favor, responde todas las preguntas antes de enviar.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 2. Calculamos los puntos totales del test
            val totalPuntos = calcularPuntuacion()

            // 3. Evaluamos según los rangos clínicos estandarizados del SOGS
            val diagnostico = when {
                totalPuntos in 0..2 -> "Sin problemas con el juego"
                totalPuntos in 3..4 -> "Juego de riesgo"
                else -> "Juego patológico (Posible ludopatía)"
            }

            // 4. Capturar la reflexión escrita por el usuario
            val comentarioTexto = editComentarios.text.toString().trim()
            val comentarioFinal = if (comentarioTexto.isEmpty()) "Sin comentarios registrados." else comentarioTexto

            // 5. Armar el objeto para Ktor
            val preferenciasSesion = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
            val idUsuarioActivo = preferenciasSesion.getInt("USUARIO_ID", -1)

            if (idUsuarioActivo == -1) {
                Toast.makeText(this, "Error de sesión: Vuelve a ingresar a la aplicación.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val encuestaData = RegistroEncuestaDto(
                usuarioID = idUsuarioActivo,
                satisfaccion = totalPuntos, // 'satisfaccion' es el nombre que le diste a la columna en Ktor
                clasificacion = diagnostico,
                comentarios = comentarioFinal
            )

            // Deshabilitar botón temporalmente para evitar clicks duplicados en la red
            btnCalcular.isEnabled = false

            // 6. ENVIAR DATOS A KTOR EN SEGUNDO PLANO
            apiService.guardarEncuestaServidor(encuestaData).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    btnCalcular.isEnabled = true

                    if (response.isSuccessful) {
                        val mensajeExito = "Puntos: $totalPuntos\nDiagnóstico: $diagnostico\n\n¡Resultados sincronizados con éxito!"
                        Toast.makeText(this@EncuestaAct, mensajeExito, Toast.LENGTH_LONG).show()

                        editComentarios.text?.clear()
                        finish() // Regresa al Dashboard de forma automática actualizando los datos
                    } else {
                        Toast.makeText(this@EncuestaAct, "Error en el servidor backend: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    btnCalcular.isEnabled = true
                    Toast.makeText(this@EncuestaAct, "Fallo de conexión a la API: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
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

    private fun preguntasNulas(): Boolean {
        for ((grupo, _) in obtenerRespuestas()) {
            if (grupo == null || grupo.checkedRadioButtonId == -1) {
                return false
            }
        }
        return true
    }

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