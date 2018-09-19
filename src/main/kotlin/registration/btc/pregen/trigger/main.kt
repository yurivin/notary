@file:JvmName("BtcPreGenerationTriggerMain")

package registration.btc.pregen.trigger

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import config.loadConfigs
import model.IrohaCredential
import mu.KLogging
import provider.TriggerProvider
import provider.btc.BtcSessionProvider
import registration.btc.pregen.BtcPreGenConfig
import registration.btc.pregen.BtcPreGenCredentials
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil
import util.getRandomId

private val logger = KLogging().logger

private val PREFIX = "btc-pregen"

/**
This function is used to start BTC multisignature addresses pregeneration
 */
fun main(args: Array<String>) {
    val btcPkPreGenConfig =
        loadConfigs(PREFIX, BtcPreGenConfig::class.java, "/btc/pregeneration.properties")
    val credentials =
        loadConfigs(PREFIX, BtcPreGenCredentials::class.java, "/btc/pregeneration_credentials.properties")
    executeTrigger(btcPkPreGenConfig, credentials)
}

fun executeTrigger(btcPkPreGenConfig: BtcPreGenConfig, credentialsConfig: BtcPreGenCredentials) {
    logger.info { "Run BTC multisignature address pregeneration trigger" }

    val callerCredential = IrohaCredential(
        credentialsConfig.btcSessionCreatorCredentials.accountId, ModelUtil.loadKeypair(
            credentialsConfig.btcSessionCreatorCredentials.pubKeyPath,
            credentialsConfig.btcSessionCreatorCredentials.privKeyPath
        ).get()
    )
    IrohaInitialization.loadIrohaLibrary()
        .flatMap {
            val triggerProvider = TriggerProvider(
                btcPkPreGenConfig.iroha,
                callerCredential,
                btcPkPreGenConfig.pubKeyTriggerAccount
            )
            val btcKeyGenSessionProvider = BtcSessionProvider(
                btcPkPreGenConfig.iroha,
                callerCredential,
                btcPkPreGenConfig.sessionsDomain
            )
            val sessionAccountName = String.getRandomId()
            btcKeyGenSessionProvider.createPubKeyCreationSession(sessionAccountName)
                .map { triggerProvider.trigger(sessionAccountName) }
        }.failure { ex ->
            logger.error("Cannot trigger btc address pregeneration", ex)
            System.exit(1)
        }
}
