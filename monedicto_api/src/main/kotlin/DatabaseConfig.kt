package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.StdOutSqlLogger

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
    val comentarios = varchar("comentarios", 8000)        // Último comentario o reflexión
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
<<<<<<< HEAD
    // ⚠️ REEMPLAZA: 'localhost' por la IP de tu PC si es necesario, e ingresa tu usuario y contraseña de SQL Server
    Database.connect(
        url = "jdbc:sqlserver://localhost:1433;databaseName=monedicto;encrypt=false;trustServerCertificate=true;",
        driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        user = "Chang",
        password = "ch1234"
    )
=======
    System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    System.err.println("DEBUG: ENTRANDO A INITDATABASE TOTALMENTE EN VIVO")
    System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
>>>>>>> 497c80fe929426d299f0b3630468fabbc585b5c2

    try {
        Database.connect(
            url = "jdbc:sqlserver://127.0.0.1:1433;database=monedicto_db;encrypt=false;trustServerCertificate=true;",
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            user = "sa",
            password = "12345"
        )

        // 2. Forzamos la ejecución inmediata fuera del Logger normal
        transaction {
            addLogger(StdOutSqlLogger)
            //SchemaUtils.drop(UsuariosTable, AhorrosTable, EncuestasTable)

            SchemaUtils.create(UsuariosTable, AhorrosTable, EncuestasTable)
            commit()
        }

        System.err.println("✅ ENHORABUENA: ¡Transacción completada en el motor SQL!")

    } catch (error: Throwable) {
        System.err.println("🛑 ERROR DETECTADO DIRECTAMENTE EN EL DRIVER DE SQL:")
        error.printStackTrace()
        // Esto obligará a la API a cerrarse con el error visible si falla
        throw RuntimeException("Fallo crítico de base de datos", error)
    }
}