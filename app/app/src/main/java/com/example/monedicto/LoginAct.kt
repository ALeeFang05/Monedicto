package com.example.monedicto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginAct : AppCompatActivity() {

    private lateinit var editLoginEmail: TextInputEditText
    private lateinit var editLoginPassword: TextInputEditText
    private lateinit var btnLoginIngresar: Button
    private lateinit var txtLoginIrARegistro: TextView

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://172.16.132.20:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MonedictoApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editLoginEmail = findViewById(R.id.editLoginEmail)
        editLoginPassword = findViewById(R.id.editLoginPassword)
        btnLoginIngresar = findViewById(R.id.btnLoginIngresar)
        txtLoginIrARegistro = findViewById(R.id.txtLoginIrARegistro)

        // Botón para procesar las credenciales
        btnLoginIngresar.setOnClickListener {
            procesarLogin()
        }

        // Texto inferior por si no tiene cuenta, mandarlo a la pantalla que creamos antes
        txtLoginIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroAct::class.java)
            startActivity(intent)
        }
    }

    private fun procesarLogin() {
        val email = editLoginEmail.text.toString().trim()
        val password = editLoginPassword.text.toString().trim()

        if (email.isEmpty()) {
            editLoginEmail.error = "Ingresa tu correo electrónico"
            editLoginEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editLoginPassword.error = "Ingresa tu contraseña"
            editLoginPassword.requestFocus()
            return
        }

        val loginData = LoginRequestDto(email = email, password = password)
        btnLoginIngresar.isEnabled = false

        apiService.loginUsuarioServidor(loginData).enqueue(object : Callback<LoginResponseDto> {
            override fun onResponse(call: Call<LoginResponseDto>, response: Response<LoginResponseDto>) {
                btnLoginIngresar.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!

                    // Guardamos el ID del usuario de la BD de SQL Server localmente para usarlo en futuras llamadas
                    val preferencias = getSharedPreferences("MONEDICTO_SESION", MODE_PRIVATE)
                    preferencias.edit().putInt("USUARIO_ID", usuario.id).apply()

                    Toast.makeText(this@LoginAct, "¡Bienvenido de vuelta, ${usuario.nombre}!", Toast.LENGTH_SHORT).show()

                    // Mandar al Dashboard principal
                    val intent = Intent(this@LoginAct, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginAct, "Credenciales incorrectas. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponseDto>, t: Throwable) {
                btnLoginIngresar.isEnabled = true
                Toast.makeText(this@LoginAct, "Error de red: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}