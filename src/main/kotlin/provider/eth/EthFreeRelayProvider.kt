package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import config.IrohaConfig
import jp.co.soramitsu.iroha.Keypair
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

/**
 * Provides with free ethereum relay wallet
 * @param irohaConfig - configuration of Iroha client
 * @param queryCreator - account to make queries of free relays
 * @param relayStorageAccount - account with relay addresses
 * @param relaySetterAccount - relay setter account
 */
// TODO D3-378 Bulat: Prevent double relay accounts usage (in perfect world it is on Iroha side with custom code).
class EthFreeRelayProvider(
    private val irohaConfig: IrohaConfig,
    private val queryCreator: IrohaCredential,
    private val relayStorageAccount: String,
    private val relaySetterAccount: String
) {

    init {
        EthRelayProviderIrohaImpl.logger.info {
            "Init free relay provider with creator '${queryCreator.accountId}' and registration account '$relaySetterAccount'"
        }
    }

    private val irohaNetwork = IrohaNetworkImpl(irohaConfig.hostname, irohaConfig.port)
    /**
     * Get first free ethereum relay wallet.
     * @return free ethereum relay wallet
     */
    fun getRelay(): Result<String, Exception> {
        return getRelays().map { freeWallets -> freeWallets.first() }
    }

    /**
     * Get all free Ethereum relay wallets
     * @return free Ethereum relay wallets
     */
    fun getRelays(): Result<Set<String>, Exception> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            relayStorageAccount,
            relaySetterAccount
        ).map { relays ->
            val freeWallets = relays.filterValues { irohaAccount -> irohaAccount == "free" }.keys
            if (freeWallets.isEmpty())
                throw IllegalStateException("EthFreeRelayProvider - no free relay wallets created by $relaySetterAccount")
            else
                freeWallets
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
