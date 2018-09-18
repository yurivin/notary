@file:JvmName("VacuumRelayMain")

package vacuum

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import config.loadEthPasswords
import mu.KLogging
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil

private const val RELAY_VACUUM_PREFIX = "relay-vacuum"
private val logger = KLogging().logger
/**
 * Entry point for moving all currency from relay contracts to master contract
 */
fun main(args: Array<String>) {
    val relayVacuumConfig = loadConfigs(RELAY_VACUUM_PREFIX, RelayVacuumConfig::class.java, "/eth/vacuum.properties")
    val relayVacuumCredentials =
        loadConfigs(RELAY_VACUUM_PREFIX, RelayVacuumCredentials::class.java, "/eth/vacuum_credentials.properties")
    executeVacuum(relayVacuumConfig, relayVacuumCredentials, args)
}

fun executeVacuum(
    relayVacuumConfig: RelayVacuumConfig,
    relayVacuumCredentials: RelayVacuumCredentials,
    args: Array<String> = emptyArray()
) {
    logger.info { "Run relay vacuum" }
    val passwordConfig = loadEthPasswords(RELAY_VACUUM_PREFIX, "/eth/ethereum_password.properties", args)
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { keypair -> RelayVacuum(relayVacuumConfig, passwordConfig, relayVacuumCredentials).vacuum() }
        .failure { ex ->
            logger.error("Cannot run vacuum", ex)
            System.exit(1)
        }
}
