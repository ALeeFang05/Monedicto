package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    // Configuración nativa del motor Netty en Ktor 3.x
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization() // Habilita el traductor de JSON

    initDatabase()           // Conecta a SQL Server y genera las tablas

    configureRouting()       // Activa las rutas/endpoints para Android
}
