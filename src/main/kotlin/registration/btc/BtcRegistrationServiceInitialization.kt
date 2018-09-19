package registration.btc

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import jp.co.soramitsu.iroha.Keypair
import model.IrohaCredential
import mu.KLogging
import provider.btc.BtcAddressesProvider
import provider.btc.BtcRegisteredAddressesProvider
import registration.RegistrationServiceEndpoint
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil

class BtcRegistrationServiceInitialization(
    private val btcRegistrationConfig: BtcRegistrationConfig,
    private val btcRegistrationCredentials: BtcRegistrationCredentials
) {

    private val queryCreator =
        IrohaCredential(
            btcRegistrationCredentials.queryCreatorCredentials.accountId, ModelUtil.loadKeypair(
                btcRegistrationCredentials.queryCreatorCredentials.pubKeyPath,
                btcRegistrationCredentials.queryCreatorCredentials.privKeyPath
            ).get()
        )


    /**
     * Init Registration Service
     */
    fun init(): Result<Unit, Exception> {
        logger.info { "Init BTC client registration service" }
        return Result.of {
            val btcAddressesProvider =
                BtcAddressesProvider(
                    btcRegistrationConfig.iroha,
                    queryCreator,
                    btcRegistrationConfig.btcAddressStorageAccount,
                    btcRegistrationConfig.btcAddressSetterAccount
                )
            val btcTakenAddressesProvider =
                BtcRegisteredAddressesProvider(
                    btcRegistrationConfig.iroha,
                    queryCreator,
                    btcRegistrationConfig.btcRegisteredAddressStorageAccount,
                    btcRegistrationConfig.btcRegisteredAddressSetterAccount
                )
            BtcRegistrationStrategyImpl(
                btcAddressesProvider,
                btcTakenAddressesProvider,
                irohaConsumer,
                btcRegistrationConfig.iroha.creator,
                btcRegistrationConfig.registrationAccount
            )
        }.map { registrationStrategy ->
            RegistrationServiceEndpoint(
                btcRegistrationConfig.port,
                registrationStrategy
            )
            Unit
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}

