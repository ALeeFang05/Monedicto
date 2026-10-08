package com.example.monedicto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
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
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        val btnMenuToolbar = findViewById<Button>(R.id.btnMenuToolbar)
        val headerView = navigationRail.headerView
        val btnLogo = headerView?.findViewById<Button>(R.id.btnLogo)

        btnMenuToolbar.setOnClickListener {
            btnMenuToolbar.visibility = View.GONE
            navigationRail.visibility = View.VISIBLE
        }

        btnMenuToolbar.setOnClickListener {
            btnMenuToolbar.visibility = View.GONE
            navigationRail.visibility = View.VISIBLE
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
    //Para que quede marcada el inicio como default
    override fun onResume() {
        super.onResume()
        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        navigationRail.selectedItemId = R.id.rail_home
    }
}


