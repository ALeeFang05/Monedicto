package com.example.monedicto

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
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

    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finanzas)

        // CONECTAR ELEMENTOS DEL XML

        editConcepto = findViewById(R.id.editConcepto)
        editMonto = findViewById(R.id.editMonto)
        editTiempo = findViewById(R.id.editTiempo)

        radioMeses = findViewById(R.id.radioMeses)
        radioAnios = findViewById(R.id.radioAnios)

        txtTotalAhorrado = findViewById(R.id.txtTotalAhorrado)

        btnGuardarAhorro = findViewById(R.id.btnGuardarAhorro)


        // MOSTRAR EL TOTAL QUE YA ESTABA GUARDADO

        mostrarTotal()


        // CUANDO SE PRESIONA EL BOTÓN

        btnGuardarAhorro.setOnClickListener {

            guardarAhorro()
        }
    }


    private fun guardarAhorro() {

        val concepto = editConcepto.text.toString().trim()

        val montoTexto = editMonto.text.toString().trim()

        val tiempoTexto = editTiempo.text.toString().trim()


        // VALIDAR CONCEPTO

        if (concepto.isEmpty()) {

            editConcepto.error = "Escribe un concepto"

            editConcepto.requestFocus()

            return
        }


        // VALIDAR MONTO

        val monto = montoTexto.toDoubleOrNull()

        if (monto == null || monto <= 0) {

            editMonto.error = "Ingresa un monto válido"

            editMonto.requestFocus()

            return
        }


        // VALIDAR TIEMPO

        val tiempo = tiempoTexto.toIntOrNull()

        if (tiempo == null || tiempo <= 0) {

            editTiempo.error = "Ingresa un tiempo válido"

            editTiempo.requestFocus()

            return
        }


        val periodo = if (radioMeses.isChecked) {
            "meses"
        } else {
            "años"
        }


        /*
         * GUARDAMOS EL DINERO EN CENTAVOS
         *
         * Ejemplo:
         *
         * $500.00 = 50000 centavos
         *
         * Esto evita problemas con decimales.
         */

        val nuevoMontoCentavos =
            (monto * 100).roundToLong()


        val preferencias =
            getSharedPreferences(
                "MONEDICTO_AHORROS",
                MODE_PRIVATE
            )


        // LEER TOTAL ANTERIOR

        val totalAnterior =
            preferencias.getLong(
                "TOTAL_AHORRADO",
                0L
            )


        // SUMAR NUEVO DEPÓSITO

        val nuevoTotal =
            totalAnterior + nuevoMontoCentavos


        // GUARDAR NUEVO TOTAL

        preferencias
            .edit()
            .putLong(
                "TOTAL_AHORRADO",
                nuevoTotal
            )
            .apply()


        // MOSTRAR TOTAL ACTUALIZADO

        txtTotalAhorrado.text =
            formatoMoneda.format(
                nuevoTotal / 100.0
            )


        // MENSAJE

        Toast.makeText(
            this,
            "Ahorro guardado: $concepto - " +
                    formatoMoneda.format(monto) +
                    " durante $tiempo $periodo",
            Toast.LENGTH_LONG
        ).show()


        // LIMPIAR TODAS LAS CASILLAS

        editConcepto.text?.clear()

        editMonto.text?.clear()

        editTiempo.text?.clear()


        // REGRESAR A MESES

        radioMeses.isChecked = true


        // PONER CURSOR OTRA VEZ EN CONCEPTO

        editConcepto.requestFocus()
    }


    private fun mostrarTotal() {

        val preferencias =
            getSharedPreferences(
                "MONEDICTO_AHORROS",
                MODE_PRIVATE
            )


        val totalGuardado =
            preferencias.getLong(
                "TOTAL_AHORRADO",
                0L
            )


        txtTotalAhorrado.text =
            formatoMoneda.format(
                totalGuardado / 100.0
            )
    }
}