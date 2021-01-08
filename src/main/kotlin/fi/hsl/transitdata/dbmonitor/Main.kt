package fi.hsl.transitdata.dbmonitor

import fi.hsl.transitdata.dbmonitor.config.ConfigParser
import mu.KotlinLogging
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}
fun main() {
    startScheduler()
}

fun startScheduler(){
    val scheduler = Executors.newScheduledThreadPool(1)
    val config = ConfigParser.createConfig()
    scheduler.scheduleWithFixedDelay(Runnable {
        try{
            MonitorService.start(config, createPubtransConnection())
            log.info("Db connection check complete")
        }
        catch(e : Exception){
            log.error("Error while checking the databases:",e)
        }
    }, 0 ,5 , TimeUnit.MINUTES)
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