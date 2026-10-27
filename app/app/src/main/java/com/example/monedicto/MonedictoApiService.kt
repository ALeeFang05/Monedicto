package com.example.monedicto

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Modelos espejo que recibirá Android desde el Ktor
data class DashboardResponse(
    val ahorro: AhorroDto,
    val encuestaSogs: SogsDto
)
data class AhorroDto(
    val montoMeta: Double,
    val montoActual: Double,
    val porcentajeProgreso: Int,
    val fechaInicio: String,
    val fechaFin: String
)

data class SogsDto(
    val usuarioID: Int,
    val puntuacionTotal: Int,
    val clasificacion: String,
    val totalOpinionesRealizadas: Int,
    val ultimoComentario: String
)

// 1. Objeto de transferencia idéntico a las columnas que espera AhorrosTable en Ktor
data class AhorroPostRequest(
    val usuarioID: Int,
    val concepto: String,
    val monto: Double,
    val montoMeta: Double,
    val fechaInicio: String,
    val fechaFin: String
)
data class RegistroEncuestaDto(
    val usuarioID: Int,
    val satisfaccion: Int,        // Mapea la puntuación total (0 a 20)
    val clasificacion: String,    // "Juego Patológico", "Juego de Riesgo", etc.
    val comentarios: String
)
data class RegistroUsuarioDto(
    val nombre: String,
    val email: String,
    val password: String
)
data class LoginRequestDto(
    val email: String,
    val password: String
)
data class LoginResponseDto(
    val id: Int,
    val nombre: String,
    val email: String
)

// 2. Interfaz de endpoints para Retrofit
interface MonedictoApiService {
    @POST("api/finanzas/ahorros")
    fun guardarAhorroServidor(@Body request: AhorroPostRequest): Call<Void>

    @POST("api/encuesta")
    fun guardarEncuestaServidor(@Body request: RegistroEncuestaDto): Call<Void>

    @POST("api/usuarios/registro")
    fun registrarUsuarioServidor(@Body request: RegistroUsuarioDto): Call<Void>

    @POST("api/usuarios/login")
    fun loginUsuarioServidor(@Body request: LoginRequestDto): Call<LoginResponseDto>

    @GET("api/dashboard")
    fun obtenerDashboard(@Query("usuarioId") usuarioId: Int): Call<DashboardResponse>
}