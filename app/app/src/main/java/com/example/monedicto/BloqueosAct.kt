package com.example.monedicto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class BloqueosAct : AppCompatActivity() {

    private lateinit var switchBloqueoApps: SwitchMaterial
    private lateinit var switchBloqueoDns: SwitchMaterial
    private lateinit var listaAppsInstaladas: ListView

    data class AppInfo(val nombre: String, val paquete: String, val icono: Drawable)
    private val appsDisponibles = mutableListOf<AppInfo>()
    private val paquetesBloqueados = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueos)

        switchBloqueoApps = findViewById(R.id.switchBloqueoApps)
        switchBloqueoDns = findViewById(R.id.switchBloqueoDns)
        listaAppsInstaladas = findViewById(R.id.listaAppsInstaladas)

        val prefs = getSharedPreferences("MONEDICTO_BLOQUEOS", MODE_PRIVATE)
        switchBloqueoApps.isChecked = prefs.getBoolean("BLOQUEO_APPS_ACTIVO", false)
        switchBloqueoDns.isChecked = prefs.getBoolean("BLOQUEO_DNS_ACTIVO", false)

        val guardados = prefs.getStringSet("PAQUETES_BLOQUEADOS", emptySet()) ?: emptySet()
        paquetesBloqueados.addAll(guardados)

        cargarListaDeAplicaciones(prefs)

        switchBloqueoApps.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("BLOQUEO_APPS_ACTIVO", isChecked).apply()
            if (isChecked) {
                AlertDialog.Builder(this)
                    .setTitle("Permiso Requerido")
                    .setMessage("Activa 'Monedicto' en los servicios de Accesibilidad para bloquear las apps seleccionadas.")
                    .setPositiveButton("Ir a Ajustes") { _, _ ->
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        switchBloqueoApps.isChecked = false
                    }
                    .show()
            } else {
                Toast.makeText(this, "Bloqueo de apps pausado", Toast.LENGTH_SHORT).show()
            }
        }

        switchBloqueoDns.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("BLOQUEO_DNS_ACTIVO", isChecked).apply()
            if (isChecked) {
                val dnsSeguro = "://cloudflare-dns.com"
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("DNS Seguro", dnsSeguro))

                AlertDialog.Builder(this)
                    .setTitle("Activar Filtro DNS")
                    .setMessage("Hemos copiado '$dnsSeguro' a tu portapapeles.\n\nEn la siguiente pantalla, busca 'DNS Privado', elige 'Nombre de host del proveedor' y pega la dirección.")
                    .setPositiveButton("Abrir Ajustes de Red") { _, _ ->
                        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            } else {
                Toast.makeText(this, "Recuerda desactivar el DNS Privado en tus ajustes de red", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarListaDeAplicaciones(prefs: android.content.SharedPreferences) {
        val pm = packageManager
        val intentPrincipal = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val paquetes = pm.queryIntentActivities(intentPrincipal, 0)
        for (info in paquetes) {
            val paquete = info.activityInfo.packageName
            if (paquete != packageName) {
                val nombreApp = info.loadLabel(pm).toString()
                val iconoApp = info.loadIcon(pm)
                appsDisponibles.add(AppInfo(nombreApp, paquete, iconoApp))
            }
        }

        appsDisponibles.sortBy { it.nombre.lowercase() }

        val adapter = AppsAdapter()
        listaAppsInstaladas.adapter = adapter

        listaAppsInstaladas.setOnItemClickListener { _, _, position, _ ->
            val appSeleccionada = appsDisponibles[position]
            if (paquetesBloqueados.contains(appSeleccionada.paquete)) {
                paquetesBloqueados.remove(appSeleccionada.paquete)
            } else {
                paquetesBloqueados.add(appSeleccionada.paquete)
            }
            prefs.edit().putStringSet("PAQUETES_BLOQUEADOS", paquetesBloqueados).apply()
            adapter.notifyDataSetChanged()
        }
    }

    inner class AppsAdapter : BaseAdapter() {
        override fun getCount(): Int = appsDisponibles.size
        override fun getItem(position: Int): Any = appsDisponibles[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@BloqueosAct)
                .inflate(R.layout.item_app_bloqueo, parent, false)

            val app = appsDisponibles[position]
            val imgIcono = view.findViewById<ImageView>(R.id.imgIconoApp)
            val txtNombre = view.findViewById<TextView>(R.id.txtNombreApp)
            val chkBloquear = view.findViewById<CheckBox>(R.id.chkBloquearApp)

            imgIcono.setImageDrawable(app.icono)
            txtNombre.text = app.nombre
            chkBloquear.isChecked = paquetesBloqueados.contains(app.paquete)

            return view
        }
    }
}