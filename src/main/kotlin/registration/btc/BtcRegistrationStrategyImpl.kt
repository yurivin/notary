package registration.btc

import com.github.kittinunf.result.Result
import org.bitcoinj.wallet.Wallet
import registration.IrohaAccountCreator
import registration.RegistrationStrategy
import sidechain.iroha.consumer.IrohaConsumer
import java.io.File

/**
 * Registration strategy for Bitcoin sidechain clients
 * @param irohaConsumer - iroha client
 * @param mappingAccount - account to store list of bitcoin addresses
 * @param creator - bitcoin registration account in Iroha
 * @param walletFilePath - path where bitcoinj wallet is stored
 */
class BtcRegistrationStrategyImpl(
    irohaConsumer: IrohaConsumer,
    mappingAccount: String,
    creator: String,
    walletFilePath: String
) : RegistrationStrategy {
    private val walletFile = File(walletFilePath)
    private val wallet = Wallet.loadFromFile(walletFile)

    private val irohaAccountCreator = IrohaAccountCreator(irohaConsumer, mappingAccount, creator, "bitcoin_wallet")

    /**
     * Register account and assign new bitcoin address
     * @param name - account name in Iroha
     * @param pubkey - public key of the account
     * @return associated address
     */
    override fun register(name: String, pubkey: String): Result<String, Exception> {
        val btcAddress = wallet.freshReceiveAddress().toString()
        wallet.saveToFile(walletFile)
        return irohaAccountCreator.create(btcAddress, name, pubkey)
    }
}
