package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import config.IrohaConfig
import jp.co.soramitsu.iroha.Keypair
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.consumer.IrohaNetwork
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.ModelUtil
import sidechain.iroha.util.getAccountDetails
import sidechain.iroha.util.getAssetPrecision

/**
 * Implementation of [EthTokensProvider] with Iroha storage.
 *
 * @param irohaConfig - Iroha configuration
 * @param tokenCreatorAccount - account that is a creator of assets, and credentials
 * @param tokenStorageAccount - tokenStorageAccount that holds tokens in mappingAccount account
 */
class EthTokensProviderImpl(
    private val irohaNetwork: IrohaNetwork,
    private val queryCreator: IrohaCredential,
    private val tokenCreatorAccount: String,
    private val tokenStorageAccount: String
) : EthTokensProvider {

    init {
        EthRelayProviderIrohaImpl.logger.info {
            "Init token provider with token creator '${queryCreator.accountId}' and token storage account '$tokenStorageAccount'"
        }
    }

    private val domain = "ethereum"

    /**
     * Get Ethereum tokens registered in the system
     * Query creator must have permissions for details of tokenStorage and get assets in ethereum domain.
     */
    override fun getTokens(): Result<Map<String, EthTokenInfo>, Exception> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            tokenStorageAccount,
            tokenCreatorAccount
        )
            .map {
                it.mapValues { (_, name) ->
                    getAssetPrecision(
                        queryCreator,
                        irohaNetwork,
                        "$name#$domain"
                    ).fold(
                        { precision ->
                            EthTokenInfo(name, precision)
                        },
                        { ex -> throw ex }
                    )
                }
            }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
