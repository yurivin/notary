package provider

import config.IrohaConfig
import jp.co.soramitsu.iroha.Keypair
import model.IrohaCredential
import mu.KLogging
import provider.eth.EthRelayProviderIrohaImpl
import sidechain.iroha.consumer.IrohaNetwork
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

/**
 * Provides a list of all notaries peers in the system
 * @param irohaNetwork - iroha network
 * @param queryCreator - credentials to make queries
 * @param notaryListStorageAccount - account with notary list
 * @param notaryListSetterAccount - notary setter account
 */
class NotaryPeerListProviderImpl(
    private val irohaNetwork: IrohaNetwork,
    private val queryCreator: IrohaCredential,
    private val notaryListStorageAccount: String,
    private val notaryListSetterAccount: String
) : NotaryPeerListProvider {

    init {
        EthRelayProviderIrohaImpl.logger.info {
            "Init notary peer list provider with notary list storage account '$notaryListStorageAccount'" +
                    " and notary list setter account '$notaryListSetterAccount'"
        }
    }

    override fun getPeerList(
    ): List<PeerAddress> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            notaryListStorageAccount,
            notaryListSetterAccount
        ).fold(
            { notaries -> notaries.values.toList() },
            { ex -> throw ex })
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
