@file:JvmName("BtcRegistrationMain")

package registration.btc

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import mu.KLogging
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil

private val logger = KLogging().logger
private val PREFIX = "btc-registration"

/**
 * Entry point for Registration Service
 */
fun main(args: Array<String>) {
    val registrationConfig =
        loadConfigs(PREFIX, BtcRegistrationConfig::class.java, "/btc/registration.properties")
    val registrationCredentials =
        loadConfigs(PREFIX, BtcRegistrationCredentials::class.java, "/btc/registration_credentials.properties")
    executeRegistration(registrationConfig, registrationCredentials)
}

fun executeRegistration(registrationConfig: BtcRegistrationConfig, registrationCredentials: BtcRegistrationCredentials) {
    logger.info { "Run BTC client registration" }
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { BtcRegistrationServiceInitialization(registrationConfig, registrationCredentials).init() }
        .failure { ex ->
            logger.error("Cannot run btc registration", ex)
            System.exit(1)
        }
}
