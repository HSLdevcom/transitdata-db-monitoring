import config.ConfigParser

fun main() {
    val config = ConfigParser.createConfig()
    MonitorService.start(config)
}