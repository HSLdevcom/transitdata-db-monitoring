package fi.hsl.transitdata.dbmonitor

import com.typesafe.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.net.URL
import java.sql.Connection
import java.sql.Statement

object MonitorService{
    private val log = KotlinLogging.logger {}

    fun start(config : Config, connection : Connection)= runBlocking {
        try{
            connection.use {
                config.getConfigList("databases").forEach {
                    endpointToCheck -> (
                    try {
                        checkIfDbIfReachable(endpointToCheck.getString("dblabel"), endpointToCheck.getString("dbname"), it)
                    }
                    catch(e : Exception){
                        log.error ("Failed to connect to db", e)
                        sendErrorMessageToSlack(e, config)
                    })
                }
            }
        }
        catch (e: Exception){
            sendErrorMessageToSlack(e, config)
        }
    }

    private fun sendErrorMessageToSlack(e : Exception, config : Config, dblabel : String? = null){

        val url = URL(config.getString("slackbridge"))
        val headers : Map<String, String> = mapOf(
            Pair("Content-type","application/json")
        )
        NetworkHelper.getResponse(url, headers, config.getString("errormessage") + " " + e.message)
    }

    /**
     * Throws an exception if can't reach the db
     */
    private suspend fun checkIfDbIfReachable(dblabel : String, dbname : String, connection : Connection){
       withContext(Dispatchers.IO){
           try {
               val stmt: Statement = connection.createStatement()
               stmt.execute("use [$dbname]")
               stmt.close()
               assert(connection.isValid(5000))
           } catch (e: Exception) {
               throw Exception("Connection to DB $dblabel failed", e)
           }
       }
    }
}

