@file:JvmName("BtcPreGenerationMain")

package registration.btc.pregen

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import config.loadConfigs
import mu.KLogging
import sidechain.iroha.IrohaInitialization
import sidechain.iroha.util.ModelUtil

private val logger = KLogging().logger
private val PREFIX = "btc-pregen"


fun main(args: Array<String>) {
    val btcPkPreGenConfig =
        loadConfigs(PREFIX, BtcPreGenConfig::class.java, "/btc/pregeneration.properties")
    val btcPreGenCredentials =
        loadConfigs(PREFIX, BtcPreGenCredentials::class.java, "/btc/pregeneration_credentials.properties")
    executePreGeneration(btcPkPreGenConfig, btcPreGenCredentials)
}

fun executePreGeneration(btcPkPreGenConfig: BtcPreGenConfig, credentials: BtcPreGenCredentials) {
    logger.info { "Run BTC multisignature address pregeneration" }
    IrohaInitialization.loadIrohaLibrary()
        .flatMap { BtcPreGenInitialization(credentials, btcPkPreGenConfig).init() }
        .failure { ex ->
            logger.error("cannot run btc address pregeneration", ex)
            System.exit(1)
        }
}
