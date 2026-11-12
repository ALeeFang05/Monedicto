package com.example.monedicto

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
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

    // CAMPOS DE ENTRADA
    private lateinit var editConcepto: TextInputEditText
    private lateinit var editMonto: TextInputEditText
    private lateinit var editMetaAhorro: TextInputEditText
    private lateinit var editTiempo: TextInputEditText

    // RADIOBUTTONS
    private lateinit var radioMeses: RadioButton
    private lateinit var radioAnios: RadioButton

    // ELEMENTOS VISUALES
    private lateinit var txtTotalAhorrado: TextView
    private lateinit var txtInfoMetas: TextView
    private lateinit var txtPorcentajeAhorro: TextView
    private lateinit var txtFaltanteAhorro: TextView
    private lateinit var barraProgresoAhorro: ProgressBar

    private lateinit var txtFechaInicioAhorro: TextView
    private lateinit var txtFechaFinAhorro: TextView

    private lateinit var btnGuardarAhorro: Button

    // FORMATOS
    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    private val formatoFecha =
        SimpleDateFormat("dd / MMM / yyyy", Locale("es", "MX"))

    // CONEXIÓN CON SERVIDOR
    // Cambia esta IP por la IP actual de tu servidor Ktor.
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.218:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService =
        retrofit.create(MonedictoApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finanzas)

        // VINCULAR CAMPOS
        editConcepto = findViewById(R.id.editConcepto)
        editMonto = findViewById(R.id.editMonto)
        editMetaAhorro = findViewById(R.id.editMetaAhorro)
        editTiempo = findViewById(R.id.editTiempo)

        radioMeses = findViewById(R.id.radioMeses)
        radioAnios = findViewById(R.id.radioAnios)

        txtTotalAhorrado =
            findViewById(R.id.txtTotalAhorrado)

        txtInfoMetas =
            findViewById(R.id.txtInfoMetas)

        txtPorcentajeAhorro =
            findViewById(R.id.txtPorcentajeAhorro)

        txtFaltanteAhorro =
            findViewById(R.id.txtFaltanteAhorro)

        barraProgresoAhorro =
            findViewById(R.id.barraProgresoAhorro)

        txtFechaInicioAhorro =
            findViewById(R.id.txtFechaInicioAhorro)

        txtFechaFinAhorro =
            findViewById(R.id.txtFechaFinAhorro)

        btnGuardarAhorro =
            findViewById(R.id.btnGuardarAhorro)

        // MOSTRAR INFORMACIÓN GUARDADA
        mostrarTotal()

        // REGISTRAR DEPÓSITO
        btnGuardarAhorro.setOnClickListener {
            guardarAhorro()
        }
    }

    // OBTENER USUARIO ACTIVO
    private fun obtenerIdUsuario(): Int {

        val preferenciasSesion = getSharedPreferences(
            "MONEDICTO_SESION",
            MODE_PRIVATE
        )

        return preferenciasSesion.getInt("USUARIO_ID", -1)
    }

    // GUARDAR DEPÓSITO
    private fun guardarAhorro() {

        val idUsuarioActivo = obtenerIdUsuario()

        if (idUsuarioActivo == -1) {
            Toast.makeText(
                this,
                "Error de sesión. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val concepto =
            editConcepto.text.toString().trim()

        val montoTexto =
            editMonto.text.toString().trim()

        val metaTexto =
            editMetaAhorro.text.toString().trim()

        val tiempoTexto =
            editTiempo.text.toString().trim()

        // VALIDAR CONCEPTO
        if (concepto.isEmpty()) {
            editConcepto.error = "Escribe un concepto"
            editConcepto.requestFocus()
            return
        }

        // VALIDAR MONTO
        val monto = montoTexto.toDoubleOrNull()

        if (monto == null || !monto.isFinite() || monto <= 0 ||
            monto > Long.MAX_VALUE.toDouble() / 10000.0
        ) {
            editMonto.error = "Ingresa un monto válido"
            editMonto.requestFocus()
            return
        }

        // PREFERENCIAS DEL USUARIO
        val preferencias = getSharedPreferences(
            "MONEDICTO_AHORROS_USER_$idUsuarioActivo",
            MODE_PRIVATE
        )

        // RECUPERAR META ANTERIOR
        val metaAnteriorCentavos =
            preferencias.getLong("META_AHORRO", 0L)

        // SI EL USUARIO NO ESCRIBE UNA META,
        // UTILIZAMOS LA QUE YA TENÍA GUARDADA.
        val metaAhorro = if (metaTexto.isNotEmpty()) {
            metaTexto.toDoubleOrNull()
        } else {
            metaAnteriorCentavos / 100.0
        }

        // VALIDAR META
        if (metaAhorro == null ||
            !metaAhorro.isFinite() ||
            metaAhorro <= 0 ||
            metaAhorro > Long.MAX_VALUE.toDouble() / 10000.0
        ) {
            editMetaAhorro.error =
                "Ingresa una meta de ahorro válida"

            editMetaAhorro.requestFocus()
            return
        }

        // VALIDAR TIEMPO
        val tiempo = tiempoTexto.toIntOrNull()

        if (tiempo == null || tiempo <= 0 ||
            tiempo > 1200
        ) {
            editTiempo.error = "Ingresa un tiempo válido"
            editTiempo.requestFocus()
            return
        }

        // CALCULAR FECHAS
        val calendario = Calendar.getInstance()

        val fechaInicioTexto =
            formatoFecha.format(calendario.time)

        if (radioMeses.isChecked) {
            calendario.add(Calendar.MONTH, tiempo)
        } else {
            calendario.add(Calendar.YEAR, tiempo)
        }

        val fechaFinTexto =
            formatoFecha.format(calendario.time)

        // CREAR SOLICITUD PARA EL SERVIDOR
        val requestData = AhorroPostRequest(
            usuarioID = idUsuarioActivo,
            usuarioId = idUsuarioActivo,
            concepto = concepto,
            monto = monto,
            montoMeta = metaAhorro,
            fechaInicio = fechaInicioTexto,
            fechaFin = fechaFinTexto
        )

        // DESACTIVAR BOTÓN MIENTRAS SE GUARDA
        btnGuardarAhorro.isEnabled = false

        apiService.guardarAhorroServidor(requestData)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    btnGuardarAhorro.isEnabled = true

                    if (response.isSuccessful) {

                        // GUARDAR META LOCALMENTE
                        val metaCentavos =
                            (metaAhorro * 100).roundToLong()

                        preferencias.edit()
                            .putLong("META_AHORRO", metaCentavos)
                            .putString(
                                "FECHA_INICIO",
                                fechaInicioTexto
                            )
                            .putString(
                                "FECHA_FIN",
                                fechaFinTexto
                            )
                            .apply()

                        // ACTUALIZAR DEPÓSITOS
                        actualizarProgresoLocal(
                            monto,
                            concepto
                        )

                        // LIMPIAR CAMPOS
                        editConcepto.text?.clear()
                        editMonto.text?.clear()
                        editMetaAhorro.text?.clear()
                        editTiempo.text?.clear()

                        radioMeses.isChecked = true

                    } else {

                        Toast.makeText(
                            this@FinanzasAct,
                            "Error en el servidor: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Void>,
                    t: Throwable
                ) {
                    btnGuardarAhorro.isEnabled = true

                    Toast.makeText(
                        this@FinanzasAct,
                        "Fallo de conexión: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ACTUALIZAR AHORRO ACUMULADO LOCAL
    private fun actualizarProgresoLocal(
        monto: Double,
        concepto: String
    ) {

        val idUsuarioActivo = obtenerIdUsuario()

        if (idUsuarioActivo == -1) return

        val preferencias = getSharedPreferences(
            "MONEDICTO_AHORROS_USER_$idUsuarioActivo",
            MODE_PRIVATE
        )

        // CONVERTIR MONTO A CENTAVOS
        val nuevoMontoCentavos =
            (monto * 100).roundToLong()

        // LEER TOTAL ANTERIOR
        val totalAnterior =
            preferencias.getLong("TOTAL_AHORRADO", 0L)

        // EVITAR DESBORDAMIENTO
        if (totalAnterior > Long.MAX_VALUE - nuevoMontoCentavos) {
            Toast.makeText(
                this,
                "Depósito registrado, pero el total local excedió el límite.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // SUMAR NUEVO DEPÓSITO
        val nuevoTotal =
            totalAnterior + nuevoMontoCentavos

        // GUARDAR TOTAL
        preferencias.edit()
            .putLong("TOTAL_AHORRADO", nuevoTotal)
            .apply()

        // ACTUALIZAR PANTALLA
        mostrarTotal()

        Toast.makeText(
            this,
            "¡Ahorro registrado!: $concepto",
            Toast.LENGTH_LONG
        ).show()
    }

    // MOSTRAR TOTAL, META Y PROGRESO
    private fun mostrarTotal() {

        val idUsuarioActivo = obtenerIdUsuario()

        if (idUsuarioActivo == -1) {
            txtTotalAhorrado.text = formatoMoneda.format(0)
            txtInfoMetas.text = "Inicia sesión para consultar tu meta"
            txtFechaInicioAhorro.text = "-- / -- / ----"
            txtFechaFinAhorro.text = "-- / -- / ----"
            barraProgresoAhorro.progress = 0
            txtPorcentajeAhorro.text = "0% completado"
            txtFaltanteAhorro.text = ""
            return
        }

        val preferencias = getSharedPreferences(
            "MONEDICTO_AHORROS_USER_$idUsuarioActivo",
            MODE_PRIVATE
        )

        // RECUPERAR AHORROS Y META
        val totalCentavos =
            preferencias.getLong("TOTAL_AHORRADO", 0L)

        val metaCentavos =
            preferencias.getLong("META_AHORRO", 0L)

        val totalAhorrado = totalCentavos / 100.0
        val metaAhorro = metaCentavos / 100.0

        // MOSTRAR DINERO AHORRADO
        txtTotalAhorrado.text =
            formatoMoneda.format(totalAhorrado)

        if (metaAhorro > 0) {

            // CALCULAR PORCENTAJE
            val porcentaje =
                (totalAhorrado / metaAhorro) * 100.0

            // LA BARRA LLEGA HASTA 100%
            val progreso =
                porcentaje.toInt().coerceIn(0, 100)

            barraProgresoAhorro.progress = progreso

            // MOSTRAR META
            txtInfoMetas.text =
                "Meta deseada: ${formatoMoneda.format(metaAhorro)}"

            // MOSTRAR PORCENTAJE
            txtPorcentajeAhorro.text =
                String.format(
                    Locale("es", "MX"),
                    "%.1f%% completado",
                    porcentaje
                )

            // CALCULAR DINERO FALTANTE
            val faltante =
                (metaAhorro - totalAhorrado)
                    .coerceAtLeast(0.0)

            txtFaltanteAhorro.text =
                if (faltante > 0) {
                    "Te faltan ${formatoMoneda.format(faltante)}"
                } else {
                    "¡Felicidades! Alcanzaste tu meta."
                }

        } else {

            barraProgresoAhorro.progress = 0

            txtInfoMetas.text =
                "Meta de ahorro: sin establecer"

            txtPorcentajeAhorro.text =
                "0% completado"

            txtFaltanteAhorro.text =
                "Establece tu meta para comenzar"
        }

        // RECUPERAR FECHAS GUARDADAS
        txtFechaInicioAhorro.text =
            preferencias.getString(
                "FECHA_INICIO",
                "-- / -- / ----"
            )

        txtFechaFinAhorro.text =
            preferencias.getString(
                "FECHA_FIN",
                "-- / -- / ----"
            )
    }
}

