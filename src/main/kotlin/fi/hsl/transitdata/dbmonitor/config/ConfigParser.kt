package fi.hsl.transitdata.dbmonitor.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import java.io.File
import java.util.*

object ConfigParser {

    /**
     * Create a valid Config from a configuration file and environment variables using default filename "environment.conf".
     *
     * @see .createConfig
     */
    @Throws(RuntimeException::class)
    fun createConfig(): Config {
        return createConfig("environment.conf")
    }

    /**
     * Create a valid Config from a configuration file and environment variables.
     *
     *
     * If the environment variable CONFIG_PATH is set, it determines the path to the configuration
     * file. Otherwise only the other environment variables affect the configuration.
     *
     *
     * Environment variables override values in the configuration file in case of conflict.
     *
     * @return Complete and valid configuration.
     */
    @Throws(RuntimeException::class)
    fun createConfig(filename: String): Config {
        val fileConfig: Config? = parseFileConfig()
        val envConfig: Config = ConfigFactory.parseResources(filename).resolve()
        return mergeConfigs(fileConfig, envConfig)
    }

    /**
     * Parse a Config from the path given by the environment variable CONFIG_PATH. If CONFIG_PATH is
     * unset, return null.
     *
     * @return Either a configuration parsed from the given path or null.
     */
    @Throws(RuntimeException::class)
    private fun parseFileConfig(): Config? {
        var fileConfig: Config? = null
        val configPath = Optional.ofNullable(System.getenv("CONFIG_PATH"))
        if (configPath.isPresent) {
            fileConfig = try {
                ConfigFactory.parseFile(File(configPath.get())).resolve()
            } catch (e: ConfigException) {
                throw e
            }
        }
        return fileConfig
    }

    /**
     * Merge the given Configs and validate the result.
     *
     *
     * envConfig overrides any conflicting keys in fileConfig.
     *
     * @param fileConfig The Config read from a file or null.
     * @param envConfig The Config read from the environment variables.
     * @return The Config resulting from merging fileConfig and envConfig.
     */
    @Throws(RuntimeException::class)
    fun mergeConfigs(fileConfig: Config?, envConfig: Config): Config {
        val fullConfig: Config
        fullConfig = if (fileConfig != null) {
            envConfig.withFallback(fileConfig)
        } else {
            envConfig
        }
        fullConfig.resolve()
        try {
            fullConfig.checkValid(ConfigFactory.parseResources("application.conf").resolve())
        } catch (e: ConfigException.ValidationFailed) {
            throw e
        }
        return fullConfig
    }
}