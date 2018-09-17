package registration.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import provider.eth.EthFreeRelayProvider
import registration.IrohaAccountCreator
import registration.RegistrationStrategy
import sidechain.iroha.consumer.IrohaConsumer

/**
 * Effective implementation of [RegistrationStrategy]
 * @param irohaConsumer - iroha client
 * @param mappingAccount - account to put details of all accounts
 * @param creator - who will register account
 */
class EthRegistrationStrategyImpl(
    private val ethFreeRelayProvider: EthFreeRelayProvider,
    irohaConsumer: IrohaConsumer,
    mappingAccount: String,
    creator: String
) : RegistrationStrategy {
    private val irohaAccountCreator = IrohaAccountCreator(irohaConsumer, mappingAccount, creator, "ethereum_wallet")
    /**
     * Register new notary client
     * @param name - client name
     * @param pubkey - client public key
     * @return ethereum wallet has been registered
     */
    override fun register(name: String, pubkey: String): Result<String, Exception> {
        return ethFreeRelayProvider.getRelay()
            .flatMap { freeEthWallet ->
                irohaAccountCreator.create(freeEthWallet, name, pubkey)
            }
    }


}
