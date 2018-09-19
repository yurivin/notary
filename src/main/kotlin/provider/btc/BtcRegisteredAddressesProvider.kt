package provider.btc

import com.github.kittinunf.result.Result
import config.IrohaConfig
import model.IrohaCredential
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

//Class that provides all registered BTC addresses
class BtcRegisteredAddressesProvider(
    private val irohaConfig: IrohaConfig,
    private val queryCreator: IrohaCredential,
    private val btcRegisteredAddressStorageAccount: String,
    private val btcRegisteredAddressSetterAccount: String
) {
    private val irohaNetwork = IrohaNetworkImpl(irohaConfig.hostname, irohaConfig.port)

    /**
     * Get all registered btc addresses
     * @return map full of registered btc addresses (btc address -> iroha account name)
     */
    fun getRegisteredAddresses(): Result<Map<String, String>, Exception> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            btcRegisteredAddressStorageAccount,
            btcRegisteredAddressSetterAccount
        )
    }
}
