@file:JvmName("EthRegistrationMain")

package registration.eth

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import mu.KLogging
import sidechain.iroha.IrohaInitialization

/**
 * Entry point for Registration Service
 */
fun main(args: Array<String>) {
    val registrationConfig =
        loadConfigs("eth-registration", EthRegistrationConfig::class.java, "/eth/registration.properties")
    executeRegistration(registrationConfig)
}

fun executeRegistration(ethRegistrationConfig: EthRegistrationConfig) {
    val logger = KLogging()

    IrohaInitialization.loadIrohaLibrary()
        .flatMap { EthRegistrationServiceInitialization(ethRegistrationConfig).init() }
        .failure { ex ->
            logger.logger.error("cannot run eth registration", ex)
            System.exit(1)
        }
}