@file:JvmName("ERC20TokenRegistrationMain")

package token

import com.github.kittinunf.result.flatMap
import config.loadConfigs
import mu.KLogging
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil

private val logger = KLogging().logger
private val TR_PREFIX = "token-registration"

/**
 * ERC20 tokens registration entry point
 */
fun main(args: Array<String>) {
    val tokenRegistrationConfig =
        loadConfigs(
            TR_PREFIX,
            ERC20TokenRegistrationConfig::class.java,
            "/eth/token_registration.properties"
        )
    val tokenRegistrationCredentials = loadConfigs(
        TR_PREFIX,
        ERC20TokenRegistrationCredentials::class.java,
        "/eth/token_registration_credentials.properties"
    )
    executeTokenRegistration(tokenRegistrationConfig, tokenRegistrationCredentials)
}

fun executeTokenRegistration(
    tokenRegistrationConfig: ERC20TokenRegistrationConfig,
    tokenRegistrationCredentials: ERC20TokenRegistrationCredentials
) {
    logger.info { "Run ERC20 tokens registration" }
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { keypair -> ERC20TokenRegistration(tokenRegistrationCredentials, tokenRegistrationConfig).init() }
        .fold({ logger.info { "ERC20 tokens were successfully registered" } }, { ex ->
            logger.error("Cannot run ERC20 token registration", ex)
            System.exit(1)
        })
}
