import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.xml.internal.fastinfoset.util.StringArray
import com.typesafe.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.omg.CORBA.Environment
import sun.misc.ObjectInputFilter
import java.io.File
import java.net.URL
import java.sql.DriverManager
import kotlin.properties.Delegates


object MonitorService{

    private val f = JsonFactory()
    private val mapper: ObjectMapper = ObjectMapper(f).registerModule(KotlinModule())

    fun start(config : Config)= runBlocking {
        try{
            config.getConfigList("databases").forEach {
                endpointToCheck ->  checkIfDbIfReachable(endpointToCheck.getString("dbname"), endpointToCheck.getString("endpoint"))
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

        NetworkHelper.getResponse(url, headers, config.getString("errormessage") + " " +  e.message)
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

