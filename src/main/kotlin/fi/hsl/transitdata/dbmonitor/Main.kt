package fi.hsl.transitdata.dbmonitor

import fi.hsl.transitdata.dbmonitor.config.ConfigParser

fun main() {
    val config = ConfigParser.createConfig()
    MonitorService.start(config)
}