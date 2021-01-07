package fi.hsl.transitdata.dbmonitor

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.typesafe.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement


object MonitorService{

    private val f = JsonFactory()
    private val mapper: ObjectMapper = ObjectMapper(f).registerModule(KotlinModule())

    fun start(config : Config)= runBlocking {
        try{
            DriverManager.getConnection(config.getString("endpoint")).use {
                config.getConfigList("databases").forEach {
                        endpointToCheck ->  checkIfDbIfReachable(endpointToCheck.getString("dblabel"), endpointToCheck.getString("dbname"), it)
                }
            }
        }
        catch (e: Exception){
            sendErrorMessageToSlack(e, config)
        }
    }

    private fun sendErrorMessageToSlack(e : Exception, config : Config){

        val url = URL(config.getString("slackbridge"))
        val headers : Map<String, String> = mapOf(
            Pair("Content-type","application/json")
        )

        NetworkHelper.getResponse(url, headers, config.getString("errormessage") + " " + e.message)
        throw e
    }

    /**
     * Throws an exception if can't reach the db
     */
    private suspend fun checkIfDbIfReachable(dblabel : String, dbname : String, connection : Connection){
        withContext(Dispatchers.IO) {
            try {
                val stmt: Statement = connection.createStatement()
                stmt.execute("use [$dbname]")
                stmt.close()
                assert(connection.isValid(5000))
            } catch (e: Exception) {
                throw Exception("Connection to DB $dblabel failed")
            }
        }
    }
}

