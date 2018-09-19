@file:JvmName("EthRegistrationMain")

package registration.eth

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import mu.KLogging
import sidechain.iroha.IrohaInitialization

private val logger = KLogging().logger
private val PREFIX = "eth-registration"

/**
 * Entry point for Registration Service
 */
fun main(args: Array<String>) {
    val registrationConfig =
        loadConfigs(PREFIX, EthRegistrationConfig::class.java, "/eth/registration.properties")
    val credentials =
        loadConfigs(PREFIX, EthRegistrationCredentials::class.java, "/eth/registration_credentials.properties")
    executeRegistration(registrationConfig, credentials)
}

fun executeRegistration(ethRegistrationConfig: EthRegistrationConfig, credentials: EthRegistrationCredentials) {
    logger.info { "Run ETH registration service" }
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { EthRegistrationServiceInitialization(credentials, ethRegistrationConfig).init() }
        .failure { ex ->
            logger.error("cannot run eth registration", ex)
            System.exit(1)
        }
}
