package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

// Tabla para Finanzas / Ahorros
object AhorrosTable : Table("ahorros") {
    val id = integer("id").autoIncrement()
    val concepto = varchar("concepto", 150)
    val monto = double("monto")
    override val primaryKey = PrimaryKey(id)
}

// Tabla para la Encuesta
object EncuestasTable : Table("encuestas") {
    val id = integer("id").autoIncrement()
    val resultado = integer("resultado")
    val comentarios = text("comentarios")
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    // ⚠️ REEMPLAZA: 'localhost' por la IP de tu PC si es necesario, e ingresa tu usuario y contraseña de SQL Server
    Database.connect(
        url = "jdbc:sqlserver://localhost:1433;databaseName=monedicto_db;encrypt=false;trustServerCertificate=true;",
        driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        user = "sa",
        password = "12345"
    )

    // Genera las tablas automáticamente en SQL Server
    transaction {
        SchemaUtils.create(AhorrosTable, EncuestasTable) //Aca se agregan las nuevas tablas que se vayen agregando
    }
}