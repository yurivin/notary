package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaNetwork
import sidechain.iroha.util.getAccountDetails

/**
 * Implementation of [EthRelayProvider] with Iroha storage.
 * @param irohaNetwork - network layer of Iroha
 * @param queryCreator - credential for making relay address query
 * @param relayStorageAccount - account with relays
 * @param relaySetterAccount - account relay setter
 */
class EthRelayProviderIrohaImpl(
    private val irohaNetwork: IrohaNetwork,
    private val queryCreator: IrohaCredential,
    private val relayStorageAccount: String,
    private val relaySetterAccount: String
) : EthRelayProvider {

    init {
        logger.info {
            "Init relay provider with relay setter account '$relaySetterAccount' and storage account '$relayStorageAccount'"
        }
    }

    /**
     * Gets all non free relay wallets
     *
     * @return map<eth_wallet -> iroha_account> in success case or exception otherwise
     */
    override fun getRelays(): Result<Map<String, String>, Exception> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            relayStorageAccount,
            relaySetterAccount
        ).map { relays ->
            relays.filterValues { it != "free" }
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
