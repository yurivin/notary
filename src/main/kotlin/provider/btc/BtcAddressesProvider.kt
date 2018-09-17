package provider.btc

import com.github.kittinunf.result.Result
import config.IrohaConfig
import jp.co.soramitsu.iroha.Keypair
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

/**
 * @param irohaConfig - Iroha client configuration
 * @param keypair - Iroha keypair
 * @param mstRegistrationAccount - bitcoin registration account for bitcoin multi-sig addresses
 */
class BtcAddressesProvider(
    private val irohaConfig: IrohaConfig,
    private val keypair: Keypair,
    private val mstRegistrationAccount: String
) {
    private val irohaNetwork = IrohaNetworkImpl(irohaConfig.hostname, irohaConfig.port)
    /**
     * Get all registered btc addresses
     * @return map full of registered btc addresses (btc address -> iroha account name)
     */
    fun getAddresses(): Result<Map<String, String>, Exception> {
        return getAccountDetails(
            irohaConfig,
            keypair,
            irohaNetwork,
            irohaConfig.creator,
            mstRegistrationAccount
        )
    }
}
