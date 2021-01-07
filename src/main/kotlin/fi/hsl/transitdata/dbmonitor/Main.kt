package fi.hsl.transitdata.dbmonitor

import fi.hsl.transitdata.dbmonitor.config.ConfigParser
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

fun main() {
    val config = ConfigParser.createConfig()
    MonitorService.start(config, createPubtransConnection())
}

@Throws(Exception::class)
private fun createPubtransConnection(): Connection {
   //Default path is what works with Docker out-of-the-box. Override with a local file if needed
    val secretFilePath: String = System.getenv("FILEPATH_CONNECTION_STRING") ?: "/run/secrets/pubtrans_community_conn_string"
    val connectionString = Scanner(File(secretFilePath))
        .useDelimiter("\\Z").next()
    if (connectionString.isEmpty()) {
        throw Exception("Failed to find Pubtrans connection string, exiting application")
    }
    return DriverManager.getConnection(connectionString)
}