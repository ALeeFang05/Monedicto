package com.example.monedicto

import android.os.Bundle
//import android.os.PersistableBundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.widget.Button
import com.google.android.material.navigationrail.NavigationRailView

class MainActivity : AppCompatActivity() {

    private var estaExpandido = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)

        val headerView = navigationRail.headerView
        val btnLogo = headerView?.findViewById<Button>(R.id.btnLogo)

        btnLogo?.setOnClickListener {
            estaExpandido = !estaExpandido
            if (estaExpandido) {
                navigationRail.labelVisibilityMode = NavigationRailView.LABEL_VISIBILITY_LABELED
            } else {
                navigationRail.labelVisibilityMode = NavigationRailView.LABEL_VISIBILITY_UNLABELED
            }
        }

        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.rail_home -> {
                    true
                }

                R.id.rail_finanzas -> {
                    startActivity(Intent(this, FinanzasAct::class.java))
                    true
                }

                R.id.rail_encuesta -> {
                    startActivity(Intent(this, EncuestaAct::class.java))
                    true
                }

                R.id.rail_historial -> {
                    Toast.makeText(this, "Abriendo Historial...", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}


