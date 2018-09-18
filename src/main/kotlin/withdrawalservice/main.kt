@file:JvmName("WithdrawalServiceMain")

package withdrawalservice

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.EthereumPasswords
import config.loadConfigs
import config.loadEthPasswords
import mu.KLogging
import sidechain.iroha.IrohaInitialization

private val logger = KLogging().logger

/**
 * Main entry point of Withdrawal Service app
 */
fun main(args: Array<String>) {
    val withdrawalConfig = loadConfigs("withdrawal", WithdrawalServiceConfig::class.java, "/eth/withdrawal.properties")
    val passwordConfig = loadEthPasswords("withdrawal", "/eth/ethereum_password.properties", args)
    val credentialsConfig =
        loadConfigs("withdrawal", WithdrawalServiceCredentials::class.java, "/eth/withdrawal_credentials.properties")
    executeWithdrawal(withdrawalConfig, credentialsConfig, passwordConfig)
}

fun executeWithdrawal(
    withdrawalConfig: WithdrawalServiceConfig,
    credentialsConfig: WithdrawalServiceCredentials,
    passwordConfig: EthereumPasswords
) {
    logger.info { "Run withdrawal service" }
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { WithdrawalServiceInitialization(withdrawalConfig, credentialsConfig, passwordConfig).init() }
        .failure { ex ->
            logger.error("Cannot run withdrawal service", ex)
            System.exit(1)
        }
}
