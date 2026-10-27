package com.example.monedicto

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

class FinanzasAct : AppCompatActivity() {

    private lateinit var editConcepto: TextInputEditText
    private lateinit var editMonto: TextInputEditText
    private lateinit var editTiempo: TextInputEditText

    private lateinit var radioMeses: RadioButton
    private lateinit var radioAnios: RadioButton

    private lateinit var txtTotalAhorrado: TextView
    private lateinit var btnGuardarAhorro: Button

    private lateinit var txtInfoMetas: TextView
    private lateinit var txtFechaInicioAhorro: TextView
    private lateinit var txtFechaFinAhorro: TextView

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    private val formatoFecha = SimpleDateFormat("dd / MMM / yyyy", Locale("es", "MX"))

    // Asegúrate de poner tu IP completa en lugar del localhost y el puerto :8080/
    private val retrofit = Retrofit.Builder()
<<<<<<< HEAD
        .baseUrl("http://192.168.1.218:8080/")
=======
        .baseUrl("http://172.16.132.20:8080")
>>>>>>> 497c80fe929426d299f0b3630468fabbc585b5c2
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MonedictoApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas)

        editConcepto = findViewById(R.id.editConcepto)
        editMonto = findViewById(R.id.editMonto)
        editTiempo = findViewById(R.id.editTiempo)
        radioMeses = findViewById(R.id.radioMeses)
        radioAnios = findViewById(R.id.radioAnios)
        txtTotalAhorrado = findViewById(R.id.txtTotalAhorrado)
        btnGuardarAhorro = findViewById(R.id.btnGuardarAhorro)

        txtInfoMetas = findViewById(R.id.txtInfoMetas)
        txtFechaInicioAhorro = findViewById(R.id.txtFechaInicioAhorro)
        txtFechaFinAhorro = findViewById(R.id.txtFechaFinAhorro)

        mostrarTotal()

        btnGuardarAhorro.setOnClickListener {
            guardarAhorro()
        }
    }

    private fun guardarAhorro() {
        //  RECUPERAR EL ID DEL USUARIO DE LA SESIÓN ACTIVA
        val preferenciasSesion = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
        val idUsuarioActivo = preferenciasSesion.getInt("USUARIO_ID", -1)

        // Medida de seguridad preventiva por si falla la sesión
        if (idUsuarioActivo == -1) {
            Toast.makeText(this, "Error de sesión: Por favor reingresa a la aplicación.", Toast.LENGTH_LONG).show()
            return
        }

        val concepto = editConcepto.text.toString().trim()
        val montoTexto = editMonto.text.toString().trim()
        val tiempoTexto = editTiempo.text.toString().trim()

        if (concepto.isEmpty()) {
            editConcepto.error = "Escribe un concepto"
            editConcepto.requestFocus()
            return
        }

        val monto = montoTexto.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            editMonto.error = "Ingresa un monto válido"
            editMonto.requestFocus()
            return
        }

        val tiempo = tiempoTexto.toIntOrNull()
        if (tiempo == null || tiempo <= 0) {
            editTiempo.error = "Ingresa un tiempo válido"
            editTiempo.requestFocus()
            return
        }

        val periodo = if (radioMeses.isChecked) "meses" else "años"

        val calendario = Calendar.getInstance()
        val fechaInicioTexto = formatoFecha.format(calendario.time)

        if (radioMeses.isChecked) {
            calendario.add(Calendar.MONTH, tiempo)
        } else {
            calendario.add(Calendar.YEAR, tiempo)
        }
        val fechaFinTexto = formatoFecha.format(calendario.time)
        val montoMetaSimulado = monto * 3.0

<<<<<<< HEAD
        //OBTENEMOS EL ID DEL USUARIO USANDO LA MISMA LLAVE DEL LOGIN
        val preferencias = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
        val idUsuarioActual = preferencias.getInt("USUARIO_ID", -1) // 👈 Debe decir "USUARIO_ID" exactamente

        if (idUsuarioActual == -1) {
            Toast.makeText(this, "Error: No se encontró la sesión del usuario. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            return
        }

=======
        // INYECTAR EL usuarioId EN EL PAQUETE JSON ENVIADO A KTOR
>>>>>>> 497c80fe929426d299f0b3630468fabbc585b5c2
        val requestData = AhorroPostRequest(
            usuarioID = idUsuarioActivo, // Enlazamos el depósito a este perfil específico
            concepto = concepto,
            monto = monto,
            montoMeta = montoMetaSimulado,
            fechaInicio = fechaInicioTexto,
            fechaFin = fechaFinTexto,
            usuarioId = idUsuarioActual
        )

        btnGuardarAhorro.isEnabled = false

        apiService.guardarAhorroServidor(requestData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                btnGuardarAhorro.isEnabled = true
                if (response.isSuccessful) {
                    actualizarProgresoLocal(monto, concepto, tiempo, periodo, fechaInicioTexto, fechaFinTexto)
                } else {
                    Toast.makeText(this@FinanzasAct, "Error en el servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnGuardarAhorro.isEnabled = true
                Toast.makeText(this@FinanzasAct, "Fallo de conexión: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun actualizarProgresoLocal(
        monto: Double,
        concepto: String,
        tiempo: Int,
        periodo: String,
        fechaInicioTexto: String,
        fechaFinTexto: String
    ) {
        val nuevoMontoCentavos = (monto * 100).roundToLong()

        // 🛠️ FILTRADO LOCAL: Cambiamos "MONEDICTO_AHORROS" general por uno único basado en el ID
        val preferenciasSesion = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
        val idUsuarioActivo = preferenciasSesion.getInt("USUARIO_ID", -1)

        val preferencias = getSharedPreferences("MONEDICTO_AHORROS_USER_$idUsuarioActivo", MODE_PRIVATE)

        val totalAnterior = preferencias.getLong("TOTAL_AHORRADO", 0L)
        val nuevoTotal = totalAnterior + nuevoMontoCentavos

        preferencias.edit().putLong("TOTAL_AHORRADO", nuevoTotal).apply()

        txtTotalAhorrado.text = formatoMoneda.format(nuevoTotal / 100.0)

        txtInfoMetas.text = "Meta Estimada: " + formatoMoneda.format(monto * 3.0)
        txtFechaInicioAhorro.text = fechaInicioTexto
        txtFechaFinAhorro.text = fechaFinTexto

        Toast.makeText(
            this,
            "¡Ahorro asignado a tu cuenta!: $concepto",
            Toast.LENGTH_LONG
        ).show()

        editConcepto.text?.clear()
        editMonto.text?.clear()
        editTiempo.text?.clear()
        radioMeses.isChecked = true
        editConcepto.requestFocus()
    }

    private fun mostrarTotal() {
        val preferenciasSesion = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
        val idUsuarioActivo = preferenciasSesion.getInt("USUARIO_ID", -1)

        val preferencias = getSharedPreferences("MONEDICTO_AHORROS_USER_$idUsuarioActivo", MODE_PRIVATE)
        val totalGuardado = preferencias.getLong("TOTAL_AHORRADO", 0L)
        txtTotalAhorrado.text = formatoMoneda.format(totalGuardado / 100.0)
    }
}