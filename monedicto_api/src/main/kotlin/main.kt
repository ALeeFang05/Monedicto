package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization() // Habilita el traductor de JSON


    try {
        initDatabase()           // Conecta a SQL Server y genera las tablas
        println("✅ ¡Conexión exitosa y tablas creadas en SQL Server!")
    } catch (e: Exception) {
        println("❌ ERROR DE CONEXIÓN A SQL SERVER: ${e.localizedMessage}")
        e.printStackTrace()
    }

    initDatabase()           // Conecta a SQL Server y genera las tablas
    configureRouting()       // Activa las rutas/endpoints para Android
}
