package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import config.IrohaConfig
import jp.co.soramitsu.iroha.Keypair
import mu.KLogging
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

/**
 * Provides with free ethereum relay wallet
 * @param irohaConfig - configuration of Iroha client
 * @param keypair - iroha keypair
 * @param mappingAccount - Mapping account in Iroha to write down the information about free relay wallets has been added
 * @param registrationIrohaAccount - Ethereum registration account
 */
// TODO D3-378 Bulat: Prevent double relay accounts usage (in perfect world it is on Iroha side with custom code).
class EthFreeRelayProvider(
    private val irohaConfig: IrohaConfig,
    private val keypair: Keypair,
    private val mappingAccount: String,
    private val registrationIrohaAccount: String
) {

    init {
        EthRelayProviderIrohaImpl.logger.info {
            "Init free relay provider with notary account '$notaryIrohaAccount' and registration account '$registrationIrohaAccount'"
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
            irohaConfig,
            keypair,
            irohaNetwork,
            mappingAccount,
            registrationIrohaAccount
        ).map { relays ->
            val freeWallets = relays.filterValues { irohaAccount -> irohaAccount == "free" }.keys
            if (freeWallets.isEmpty())
                throw IllegalStateException("EthFreeRelayProvider - no free relay wallets created by $registrationIrohaAccount")
            else
                freeWallets
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
