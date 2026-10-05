package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

// Modelos espejo para el JSON de Android
data class RegistroAhorroDto(val concepto: String, val monto: Double)
data class RegistroEncuestaDto(val satisfaccion: Int, val comentarios: String)

fun Application.configureRouting() {
    routing {
        route("/api") {

            // POST: http://tu_ip:8080/api/finanzas/ahorrar
            post("/finanzas/ahorrar") {
                try {
                    val recibidos = call.receive<RegistroAhorroDto>()
                    transaction {
                        AhorrosTable.insert {
                            it[concepto] = recibidos.concepto
                            it[monto] = recibidos.monto
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Ahorro guardado en SQL Server")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error: ${e.localizedMessage}")
                }
            }

            // POST: http://tu_ip:8080/api/encuesta
            post("/encuesta") {
                try {
                    val recibidos = call.receive<RegistroEncuestaDto>()
                    transaction {
                        EncuestasTable.insert {
                            it[resultado] = recibidos.satisfaccion
                            it[comentarios] = recibidos.comentarios
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Encuesta guardada en SQL Server")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error: ${e.localizedMessage}")
                }
            }
        }
    }
}