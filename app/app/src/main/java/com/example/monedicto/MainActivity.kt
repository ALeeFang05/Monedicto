package com.example.monedicto

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.progressindicator.LinearProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // Menu lateral
    private lateinit var navigationRail: NavigationRailView

    // Vistas de la tarjeta de finanzas del XML principal
    private lateinit var txtMontoAhorrado: TextView
    private lateinit var txtMetaAhorro: TextView
    private lateinit var txtFechaInicio: TextView
    private lateinit var txtFechaFin: TextView
    private lateinit var progresoAhorro: LinearProgressIndicator

    // Vistas de la tarjeta de ludopatía SOGS del XML principal
    private lateinit var txtResultadoSogs: TextView
    private lateinit var txtClasificacionSogs: TextView
    private lateinit var txtTotalEncuestas: TextView
    private lateinit var txtUltimaReflexion: TextView

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.218:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MonedictoApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. CONECTAR COMPONENTES DEL DASHBOARD
        txtMontoAhorrado = findViewById(R.id.txtMontoAhorrado)
        txtMetaAhorro = findViewById(R.id.txtMetaAhorro)
        txtFechaInicio = findViewById(R.id.txtFechaInicio)
        txtFechaFin = findViewById(R.id.txtFechaFin)
        progresoAhorro = findViewById(R.id.progresoAhorro)

        txtResultadoSogs = findViewById(R.id.txtResultadoSogs)
        txtClasificacionSogs = findViewById(R.id.txtClasificacionSogs)
        txtTotalEncuestas = findViewById(R.id.txtTotalEncuestas)
        txtUltimaReflexion = findViewById(R.id.txtUltimaReflexion)

        navigationRail = findViewById(R.id.navigationRail)
        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // ⚠️ NOTA: Asegúrate de que estos IDs coincidan con los que pusiste en 'res/menu/menu_rail_iconos.xml'
                R.id.rail_home -> {
                    // Ya estás en el Dashboard principal, no es necesario abrir otra actividad
                    Toast.makeText(this, "Ya estás en el Inicio", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.rail_finanzas -> {
                    // Abre la pantalla para registrar depósitos y metas
                    val intent = Intent(this, FinanzasAct::class.java)
                    startActivity(intent)
                    true
                }
                R.id.rail_encuesta -> {
                    // Abre el test diagnóstico de control de impulsos (13 preguntas)
                    val intent = Intent(this, EncuestaAct::class.java)
                    startActivity(intent)
                    true
                }
                R.id.rail_historial -> {
                    Toast.makeText(this, "Próximamente: Historial de recaídas", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que la pantalla vuelva a estar al frente, cargamos la información más fresca de la BD
        cargarDatosDashboard()
        navigationRail.selectedItemId = R.id.rail_home

    }

    private fun cargarDatosDashboard() {
        // 2. RECUPERAR EL ID DE USUARIO LOGUEADO
        val preferencias = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
        val usuarioId = preferencias.getInt("USUARIO_ID", -1)

        // Si por alguna razón expiró la sesión o no hay ID, lo regresamos al Login por protección
        if (usuarioId == -1) {
            val intent = Intent(this, LoginAct::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 3. CONSULTAR LA API EN SEGUNDO PLANO
        apiService.obtenerDashboard(usuarioId).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val datos = response.body()!!

                    // 4. PINTAR LA INFORMACIÓN FINANCIERA EN EL UI
                    txtMontoAhorrado.text = formatoMoneda.format(datos.ahorro.montoActual)
                    txtMetaAhorro.text = "Meta: ${formatoMoneda.format(datos.ahorro.montoMeta)}"
                    txtFechaInicio.text = datos.ahorro.fechaInicio
                    txtFechaFin.text = datos.ahorro.fechaFin
                    progresoAhorro.progress = datos.ahorro.porcentajeProgreso

                    // 5. PINTAR LOS INDICADORES DE LUDOPATÍA SOGS EN EL UI
                    txtResultadoSogs.text = "${datos.encuestaSogs.puntuacionTotal} / 13 Puntos"
                    txtClasificacionSogs.text = datos.encuestaSogs.clasificacion
                    txtTotalEncuestas.text = "Evaluaciones hechas: ${datos.encuestaSogs.totalOpinionesRealizadas}"
                    txtUltimaReflexion.text = "\"${datos.encuestaSogs.ultimoComentario}\""
                } else {
                    Toast.makeText(this@MainActivity, "Error al procesar el Dashboard: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}


