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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Monedicto"
    }

    //crea el menu de los 3 punticos
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_finanzas -> {
                val intent = Intent(this, FinanzasAct::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_historial -> {
                Toast.makeText(this, "Redirreccionando al Historial... \n " +
                        "Espere por favor...", Toast.LENGTH_SHORT)
                true
            }
            R.id.menu_encuesta -> {
                val intent = Intent(this, EncuestaAct::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_perfil -> {
                Toast.makeText(this, "Redirreccionando al perfil... \n " +
                        "Espere por favor...", Toast.LENGTH_SHORT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
