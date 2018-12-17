package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaNetwork
import sidechain.iroha.util.getAccountDetails

/**
 * Provides with free ethereum relay wallet
 * @param credential - iroha credetial for queries
 * @param notaryIrohaAccount - Master notary account in Iroha to write down the information about free relay wallets has been added
 */
// TODO Prevent double relay accounts usage (in perfect world it is on Iroha side with custom code). In real world
// on provider side with some synchronization.
class EthFreeRelayProvider(
    private val credential: IrohaCredential,
    private val irohaNetwork: IrohaNetwork,
    private val notaryIrohaAccount: String,
    private val registrationIrohaAccount: String
) {

    init {
        EthRelayProviderIrohaImpl.logger.info {
            "Init free relay provider with notary account '$notaryIrohaAccount' and registration account '$registrationIrohaAccount'"
        }
    }

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
            credential,
            irohaNetwork,
            notaryIrohaAccount,
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