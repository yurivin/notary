package provider.btc

import com.github.kittinunf.result.Result
import config.IrohaConfig
import model.IrohaCredential
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.getAccountDetails

/**
 * @param irohaConfig - Iroha client configuration
 * @param queryCreator - credential for query creator
 * @param btcAddressStorageAccount - account with all btc addresses
 * @param btcAddressSetterAccount - account address creator
 */
class BtcAddressesProvider(
    private val irohaConfig: IrohaConfig,
    private val queryCreator: IrohaCredential,
    private val btcAddressStorageAccount: String,
    private val btcAddressSetterAccount: String
) {
    private val irohaNetwork = IrohaNetworkImpl(irohaConfig.hostname, irohaConfig.port)
    /**
     * Get all created btc addresses
     * @return map full of created btc addresses (btc address -> iroha account name)
     */
    fun getAddresses(): Result<Map<String, String>, Exception> {
        return getAccountDetails(
            queryCreator,
            irohaNetwork,
            btcAddressStorageAccount,
            btcAddressSetterAccount
        )
    }
}
