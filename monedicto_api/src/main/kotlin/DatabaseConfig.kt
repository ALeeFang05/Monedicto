package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

// Talba para Usuarios
object UsuariosTable : Table("usuarios") {
    val id=integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 30)
    override val primaryKey = PrimaryKey(id)
}

// Tabla para Finanzas / Ahorros
object AhorrosTable : Table("ahorros") {
    val id = integer("id").autoIncrement()
    val usuarioID = integer("usuario_ID").references(UsuariosTable.id)
    val concepto = varchar("concepto", 150)
    val monto = double("monto")                  // Dinero parcial depositado actual
    val montoMeta = double("monto_meta")         // La meta final calculada (monto * 3)
    val fechaInicio = varchar("fecha_inicio", 50) // Fecha en que se registra el depósito
    val fechaFin = varchar("fecha_fin", 50)       // Fecha estimada en que se completaría la meta
    override val primaryKey = PrimaryKey(id)
}

// Tabla para la Encuesta
object EncuestasTable : Table("encuestas") {
    val id = integer("id").autoIncrement()
    val usuarioID = integer("usuario_ID").references(UsuariosTable.id)
    val resultado = integer("resultado")         // Puntuación del test SOGS de 0 a 20
    val clasificacion = varchar("clasificacion", 100) // "Sin problema", "Juego patológico", etc.
    val comentarios = text("comentarios")        // Último comentario o reflexión
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    // ⚠️ REEMPLAZA: 'localhost' por la IP de tu PC si es necesario, e ingresa tu usuario y contraseña de SQL Server
    Database.connect(
        url = "jdbc:sqlserver://localhost:1433;databaseName=monedicto;encrypt=false;trustServerCertificate=true;",
        driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        user = "Chang",
        password = "ch1234"
    )

    // Genera las tablas automáticamente en SQL Server
    transaction {
        SchemaUtils.create(UsuariosTable, AhorrosTable, EncuestasTable) //Aca se agregan las nuevas tablas que se vayen agregando
    }
}