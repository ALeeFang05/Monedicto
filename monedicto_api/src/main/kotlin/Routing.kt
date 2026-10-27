package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.and

// 💳 DTO Actualizado para Finanzas (Sincronizado con Android)
@Serializable
data class RegistroAhorroDto(
    val usuarioID: Int,
    val concepto: String,
    val monto: Double,
    val montoMeta: Double,
    val fechaInicio: String,
    val fechaFin: String
)

// 📝 DTO Actualizado para la Encuesta SOGS de Ludopatía
@Serializable
data class RegistroEncuestaDto(
    val usuarioID: Int,
    val satisfaccion: Int,        // Aquí mapeamos la puntuación del test SOGS (0 a 20)
    val clasificacion: String,    // "Juego Patológico", "Juego de Riesgo", etc.
    val comentarios: String
)

// 📊 DTO para enviar el resumen completo al Dashboard de Android (GET)
@Serializable
data class DashboardResponse(val ahorro: AhorroDto, val encuestaSogs: SogsDto)
@Serializable
data class AhorroDto(val montoMeta: Double, val montoActual: Double, val porcentajeProgreso: Int, val fechaInicio: String, val fechaFin: String)
@Serializable
data class SogsDto(val puntuacionTotal: Int, val clasificacion: String, val totalOpinionesRealizadas: Int, val ultimoComentario: String)
@Serializable
data class RegistroUsuarioDto(val nombre: String, val email: String, val password: String)
@Serializable
data class LoginRequestDto(val email: String, val password: String)
@Serializable
data class LoginResponseDto(val id: Int, val nombre: String, val email: String)


fun Application.configureRouting() {
    routing {
        route("/api") {

            // 1. GET: http://localhost:8080/api/dashboard (Para alimentar tu pantalla principal)
            get("/dashboard") {
                try {
                    val usuarioIdParam = call.request.queryParameters["usuarioId"]?.toIntOrNull()

                    if (usuarioIdParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Falta el parámetro usuarioId")
                        return@get
                    }
                    var ahorroDto: AhorroDto? = null
                    var sogsDto: SogsDto? = null

                    transaction {
                        // Leer último ahorro de SQL Server
                        val ultimoAhorro = AhorrosTable.selectAll()
                            .where { AhorrosTable.usuarioID eq usuarioIdParam }
                            .orderBy(AhorrosTable.id to org.jetbrains.exposed.sql.SortOrder.DESC)
                            .limit(1)
                            .singleOrNull()

                        if (ultimoAhorro != null) {
                            val actual = ultimoAhorro[AhorrosTable.monto]
                            val meta = ultimoAhorro[AhorrosTable.montoMeta]
                            val porcentaje = if (meta > 0) ((actual / meta) * 100).toInt() else 0

                            ahorroDto = AhorroDto(
                                montoMeta = meta,
                                montoActual = actual,
                                porcentajeProgreso = porcentaje,
                                fechaInicio = ultimoAhorro[AhorrosTable.fechaInicio],
                                fechaFin = ultimoAhorro[AhorrosTable.fechaFin]
                            )
                        }

                        // Leer historial de encuestas SOGS
                        val encuestasUsuario = EncuestasTable.selectAll()
                            .where { EncuestasTable.usuarioID eq usuarioIdParam }
                            .toList()

                        val ultimaEncuesta = encuestasUsuario.lastOrNull()

                        if (ultimaEncuesta != null) {
                            sogsDto = SogsDto(
                                puntuacionTotal = ultimaEncuesta[EncuestasTable.resultado],
                                clasificacion = ultimaEncuesta[EncuestasTable.clasificacion],
                                totalOpinionesRealizadas = encuestasUsuario.size,
                                ultimoComentario = ultimaEncuesta[EncuestasTable.comentarios]
                            )
                        }
                    }

                    // Valores por defecto si la base de datos está vacía
                    val finalAhorro = ahorroDto ?: AhorroDto(0.0, 0.0, 0, "-- / -- / ----", "-- / -- / ----")
                    val finalSogs = sogsDto ?: SogsDto(0, "Sin evaluaciones", 0, "No hay reflexiones registradas todavía.")

                    call.respond(HttpStatusCode.OK, DashboardResponse(finalAhorro, finalSogs))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error en Dashboard: ${e.localizedMessage}")
                }
            }

            // 2. POST: http://localhost:8080/api/finanzas/ahorrar
            post("/finanzas/ahorrar") {
                try {
                    val recibidos = call.receive<RegistroAhorroDto>()
                    transaction {
                        AhorrosTable.insert {
                            it[usuarioID] = recibidos.usuarioID
                            it[concepto] = recibidos.concepto
                            it[monto] = recibidos.monto
                            it[montoMeta] = recibidos.montoMeta
                            it[fechaInicio] = recibidos.fechaInicio
                            it[fechaFin] = recibidos.fechaFin
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Ahorro guardado en SQL Server")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error: ${e.localizedMessage}")
                }
            }

            // 3. POST: http://localhost:8080/api/encuesta
            post("/encuesta") {
                try {
                    val recibidos = call.receive<RegistroEncuestaDto>()
                    transaction {
                        EncuestasTable.insert {
                            it[usuarioID] = recibidos.usuarioID
                            it[resultado] = recibidos.satisfaccion
                            it[clasificacion] = recibidos.clasificacion
                            it[comentarios] = recibidos.comentarios
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Encuesta SOGS guardada en SQL Server")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error: ${e.localizedMessage}")
                }
            }
            post("/usuarios/registro") {
                try {
                    val datos = call.receive<RegistroUsuarioDto>()
                    transaction {
                        UsuariosTable.insert {
                            it[nombre] = datos.nombre
                            it[email] = datos.email
                            it[password] = datos.password
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Usuario registrado con éxito")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error al registrar: ${e.localizedMessage}")
                }
            }
            post("/usuarios/login") {
                try {
                    val credenciales = call.receive<LoginRequestDto>()
                    var usuarioValido: LoginResponseDto? = null

                    transaction {
                        // Buscamos un usuario que coincida exactamente con el email Y la contraseña ingresados
                        val registro = UsuariosTable.selectAll()
                            .where { (UsuariosTable.email eq credenciales.email) and (UsuariosTable.password eq credenciales.password) }
                            .singleOrNull()

                        if (registro != null) {
                            usuarioValido = LoginResponseDto(
                                id = registro[UsuariosTable.id],
                                nombre = registro[UsuariosTable.nombre],
                                email = registro[UsuariosTable.email]
                            )
                        }
                    }

                    if (usuarioValido != null) {
                        // Si lo encuentra, responde con estatus 200 OK y devuelve los datos del perfil
                        call.respond(HttpStatusCode.OK, usuarioValido!!)
                    } else {
                        // Si las credenciales están mal, responde con un 401 Unauthorized
                        call.respond(HttpStatusCode.Unauthorized, "Correo o contraseña incorrectos")
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error al iniciar sesión: ${e.localizedMessage}")
                }
            }
        }
    }
}