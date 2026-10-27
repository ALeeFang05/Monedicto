package com.example.monedicto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistroAct : AppCompatActivity() {

    private lateinit var editRegNombre: TextInputEditText
    private lateinit var editRegEmail: TextInputEditText
    private lateinit var editRegPassword: TextInputEditText
    private lateinit var btnRegistrarUsuario: Button

    // ⚠️ CONFIGURACIÓN DE RETROFIT: Apunta al localhost de la PC desde el emulador
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
        setContentView(R.layout.activity_registro)

        // ENLAZAR CON LOS IDS DEL XML
        editRegNombre = findViewById(R.id.editRegNombre)
        editRegEmail = findViewById(R.id.editRegEmail)
        editRegPassword = findViewById(R.id.editRegPassword)
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario)

        btnRegistrarUsuario.setOnClickListener {
            procesarRegistro()
        }
    }

    private fun procesarRegistro() {
        val nombre = editRegNombre.text.toString().trim()
        val email = editRegEmail.text.toString().trim()
        val password = editRegPassword.text.toString().trim()

        // 1. VALIDACIONES DE CAMPOS VACÍOS
        if (nombre.isEmpty()) {
            editRegNombre.error = "Escribe tu nombre completo"
            editRegNombre.requestFocus()
            return
        }

        if (email.isEmpty()) {
            editRegEmail.error = "Ingresa un correo electrónico"
            editRegEmail.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 4) {
            editRegPassword.error = "La contraseña debe tener al menos 4 caracteres"
            editRegPassword.requestFocus()
            return
        }

        // 2. ARMAR EL OBJETO DTO
        val nuevoUsuario = RegistroUsuarioDto(
            nombre = nombre,
            email = email,
            password = password
        )

        // Deshabilitar botón para evitar peticiones duplicadas
        btnRegistrarUsuario.isEnabled = false

        // 3. ENVIAR PETICIÓN A KTOR
        apiService.registrarUsuarioServidor(nuevoUsuario).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                btnRegistrarUsuario.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@RegistroAct, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()

                    // Al registrarse con éxito, lo mandamos al Dashboard principal (MainActivity)
                    val intent = Intent(this@RegistroAct, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra la pantalla de registro para que no pueda regresar con el botón de atrás
                } else {
                    Toast.makeText(this@RegistroAct, "El correo ya está registrado o el servidor falló.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnRegistrarUsuario.isEnabled = true
                Toast.makeText(this@RegistroAct, "Error de red: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}