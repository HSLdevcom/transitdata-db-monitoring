import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.omg.CORBA.Environment
import java.io.File
import java.net.URL
import java.sql.DriverManager
import kotlin.properties.Delegates


object MonitorService{

    private val f = JsonFactory()
    private val mapper: ObjectMapper = ObjectMapper(f).registerModule(KotlinModule())

    fun start()= runBlocking {
        var monitorConf : MonitorConf? = null
        try{
            monitorConf = mapper.readValue(File("monitor.json"))
            monitorConf!!.endpoints.forEach {
                endpointToCheck ->  checkIfDbIfReachable(endpointToCheck.dbname, endpointToCheck.endpoint)
            }
        }
        catch (e: Exception){
            sendErrorMessageToSlack(e, monitorConf!!)
        }
    }

    private fun sendErrorMessageToSlack(e : Exception, monitorConf : MonitorConf){

        val url = URL(monitorConf.slackbridge)
        val headers : Map<String, String> = mapOf(
            Pair("Content-type","application/json")
        )

        NetworkHelper.getResponse(url, headers, monitorConf.errormessage + " " +  e.message)
        throw e
    }

    /**
     * Throws an exception if can't reach the db
     */
    private suspend fun checkIfDbIfReachable(dbname : String, url : String){
        withContext(Dispatchers.IO) {
            try {
                val connection = DriverManager.getConnection(url)
                assert(connection.isValid(5000))
                connection.close()
            } catch (e: Exception) {
                throw Exception("Connection to DB $dbname failed")
            }
        }
    }
}

