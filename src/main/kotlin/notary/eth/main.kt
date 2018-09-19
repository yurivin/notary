@file:JvmName("EthNotaryMain")

package notary.eth

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import config.loadEthPasswords
import model.IrohaCredential
import mu.KLogging
import provider.eth.EthRelayProviderIrohaImpl
import provider.eth.EthTokensProviderImpl
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.ModelUtil

private val logger = KLogging().logger

private val PREFIX = "eth-notary"

/**
 * Application entry point
 */
fun main(args: Array<String>) {
    val notaryConfig = loadConfigs(PREFIX, EthNotaryConfig::class.java, "/eth/notary.properties")
    val notaryCredentials = loadConfigs(PREFIX, EthNotaryCredentials::class.java, "/eth/notary_credentials.properties")
    executeNotary(notaryConfig, notaryCredentials, args)
}

fun executeNotary(
    notaryConfig: EthNotaryConfig,
    notaryCredentials: EthNotaryCredentials,
    args: Array<String> = emptyArray()
) {
    logger.info { "Run ETH notary" }
    val passwordConfig = loadEthPasswords("eth-notary", "/eth/ethereum_password.properties", args)
    val irohaNetwork = IrohaNetworkImpl(notaryConfig.iroha.hostname, notaryConfig.iroha.port)
    val queryCreator = IrohaCredential(
        notaryCredentials.queryCreatorCredentials.accountId, ModelUtil.loadKeypair(
            notaryCredentials.queryCreatorCredentials.pubKeyPath,
            notaryCredentials.queryCreatorCredentials.privKeyPath
        ).get()
    )

    val notaryCredential = IrohaCredential(
        notaryCredentials.notaryCredentials.accountId, ModelUtil.loadKeypair(
            notaryCredentials.notaryCredentials.pubKeyPath,
            notaryCredentials.notaryCredentials.privKeyPath
        ).get()
    )

    IrohaInitialization.loadIrohaLibrary()
        .flatMap {
            val ethRelayProvider = EthRelayProviderIrohaImpl(
                irohaNetwork,
                queryCreator,
                notaryConfig.relayStorageAccount,
                notaryConfig.relaySetterAccount
            )
            val ethTokensProvider = EthTokensProviderImpl(
                irohaNetwork,
                queryCreator,
                notaryConfig.tokenCreatorAccount,
                notaryConfig.tokenStorageAccount
            )
            EthNotaryInitialization(
                queryCreator,
                notaryCredential,
                notaryConfig,
                passwordConfig,
                ethRelayProvider,
                ethTokensProvider
            ).init()
        }
        .failure { ex ->
            logger.error("Cannot run eth notary", ex)
            System.exit(1)
        }
}
