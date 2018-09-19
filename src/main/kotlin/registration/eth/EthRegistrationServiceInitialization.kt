package registration.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import model.IrohaCredential
import mu.KLogging
import provider.eth.EthFreeRelayProvider
import registration.RegistrationServiceEndpoint
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil

/**
 * Initialisation of Registration Service
 *
 * @param ethRegistrationConfig - configurations of registration service
 */
class EthRegistrationServiceInitialization(
    private val ethRegistrationCredentials: EthRegistrationCredentials,
    private val ethRegistrationConfig: EthRegistrationConfig
) {

    /**
     * Init Registration Service
     */
    fun init(): Result<Unit, Exception> {
        logger.info {
            "Start registration service init with iroha creator: ${ethRegistrationCredentials.registrationServiceAccount.accountId}"
        }
        return Result.of {
            IrohaCredential(
                ethRegistrationCredentials.registrationServiceAccount.accountId, ModelUtil.loadKeypair(
                    ethRegistrationCredentials.registrationServiceAccount.pubKeyPath,
                    ethRegistrationCredentials.registrationServiceAccount.privKeyPath
                ).get()
            )
        }
            .map { credential ->
                Pair(
                    EthFreeRelayProvider(
                        ethRegistrationConfig.iroha,
                        credential,
                        ethRegistrationConfig.relayStorageAccount,
                        ethRegistrationConfig.relaySetterAccount
                    ), IrohaConsumerImpl(ethRegistrationConfig.iroha, credential)
                )
            }
            .map { (ethFreeRelayProvider, irohaConsumer) ->
                EthRegistrationStrategyImpl(
                    ethFreeRelayProvider,
                    irohaConsumer,
                    ethRegistrationConfig.defaultDomain,
                    ethRegistrationConfig.relayStorageAccount,
                    ethRegistrationCredentials.registrationServiceAccount.accountId
                )
            }
            .map { registrationStrategy ->
                RegistrationServiceEndpoint(
                    ethRegistrationConfig.port,
                    registrationStrategy
                )
            }.map { Unit }

    }


    /**
     * Logger
     */
    companion object : KLogging()

}
