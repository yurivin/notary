package registration.btc

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.fanout
import com.github.kittinunf.result.flatMap
import config.IrohaConfig
import model.IrohaCredential
import provider.btc.BtcAddressesProvider
import provider.btc.BtcRegisteredAddressesProvider
import registration.IrohaAccountCreator
import registration.RegistrationStrategy
import sidechain.iroha.consumer.IrohaConsumer
import sidechain.iroha.consumer.IrohaConsumerImpl

/**
 * Registration strategy for Bitcoin sidechain clients
 * @param btcAddressesProvider - all bitcoin addresses in the system
 * @param btcRegisteredAddressesProvider - all registered bitcoin addresses in the system
 * @param irohaConfig - Iroha configuation
 * @param btcRegisteredAddressesSetterCredential - account creator for btc
 * @param btcRegisteredAddressesStorageAccount - account with all registered accounts
 * @param accountsDomain - domain for new account
 *
 */
class BtcRegistrationStrategyImpl(
    private val btcAddressesProvider: BtcAddressesProvider,
    private val btcRegisteredAddressesProvider: BtcRegisteredAddressesProvider,
    irohaConfig: IrohaConfig,
    btcRegisteredAddressesSetterCredential: IrohaCredential,
    btcRegisteredAddressesStorageAccount: String,
    private val accountsDomain: String
) : RegistrationStrategy {


    private val irohaAccountCreator =
        IrohaAccountCreator(
            IrohaConsumerImpl(irohaConfig, btcRegisteredAddressesSetterCredential),
            btcRegisteredAddressesStorageAccount,
            btcRegisteredAddressesSetterCredential.accountId,
            "bitcoin"
        )

    /**
     * Registers new Iroha client and associates BTC address to it
     * @param name - client name
     * @param pubkey - client public key
     * @return associated BTC address
     */
    override fun register(name: String, pubkey: String): Result<String, Exception> {
        return btcAddressesProvider.getAddresses().fanout { btcRegisteredAddressesProvider.getRegisteredAddresses() }
            .flatMap { (addresses, takenAddresses) ->
                try {
                    //It fetches all BTC addresses and takes one that was not registered
                    val freeAddress = addresses.keys.first { btcAddress -> !takenAddresses.containsKey(btcAddress) }
                    irohaAccountCreator.create(freeAddress, name, accountsDomain, pubkey)
                } catch (e: NoSuchElementException) {
                    throw IllegalStateException("no free btc address to register")
                }
            }
    }
}
