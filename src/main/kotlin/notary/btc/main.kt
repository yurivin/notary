@file:JvmName("BtcNotaryMain")

package notary.btc

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import model.IrohaCredential
import mu.KLogging
import provider.btc.BtcRegisteredAddressesProvider
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil

private val logger = KLogging().logger
private val PREFIX = "btc-notary"

fun main(args: Array<String>) {
    val notaryConfig = loadConfigs(PREFIX, BtcNotaryConfig::class.java, "/btc/notary.properties")
    val notaryCredentials = loadConfigs(PREFIX, BtcNotaryCredentials::class.java, "/btc/notary_credentials.properties")
    executeNotary(notaryConfig, notaryCredentials)
}

fun executeNotary(notaryConfig: BtcNotaryConfig, notaryCredentials: BtcNotaryCredentials) {
    logger.info { "Run BTC notary" }

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
            val btcTakenAddressesProvider = BtcRegisteredAddressesProvider(
                notaryConfig.iroha,
                queryCreator,
                notaryConfig.btcRegisteredAddressStorageAccount,
                notaryConfig.btcRegisteredAddressSetterAccount
            )
            BtcNotaryInitialization(notaryConfig, notaryCredential, queryCreator, btcTakenAddressesProvider).init()
        }
        .failure { ex ->
            logger.error("Cannot run btc notary", ex)
            System.exit(1)
        }
}
