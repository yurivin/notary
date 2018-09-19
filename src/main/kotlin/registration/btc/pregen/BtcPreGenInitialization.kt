package registration.btc.pregen

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import io.reactivex.Observable
import iroha.protocol.BlockOuterClass
import iroha.protocol.Commands
import jp.co.soramitsu.iroha.Keypair
import model.IrohaCredential
import mu.KLogging
import org.bitcoinj.wallet.Wallet
import provider.NotaryPeerListProviderImpl
import provider.btc.BtcPublicKeyProvider
import registration.eth.relay.RelayRegistrationCredentials
import sidechain.iroha.IrohaChainListener
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.ModelUtil
import sidechain.iroha.util.getAccountDetails
import java.io.File

/**
 * This class listens to special trigger account and starts pregeneration process
 */
class BtcPreGenInitialization(
    btcPreGenCredentials: BtcPreGenCredentials,
    private val btcPreGenConfig: BtcPreGenConfig
) {
    private val walletFile = File(btcPreGenConfig.btcWalletFilePath)
    private val wallet = Wallet.loadFromFile(walletFile)

    private val irohaNetwork = IrohaNetworkImpl(btcPreGenConfig.iroha.hostname, btcPreGenConfig.iroha.port)

    // Account is used to make queries: Get Notraries list and blocks
    // Get account details from session Account
    private val queryCreatorCredentials =
        IrohaCredential(
            btcPreGenCredentials.queryCreatorCredentials.accountId, ModelUtil.loadKeypair(
                btcPreGenCredentials.queryCreatorCredentials.pubKeyPath,
                btcPreGenCredentials.queryCreatorCredentials.privKeyPath
            ).get()
        )

    private val btcSessionCreatorCredentials =
        IrohaCredential(
            btcPreGenCredentials.btcSessionCreatorCredentials.accountId, ModelUtil.loadKeypair(
                btcPreGenCredentials.btcSessionCreatorCredentials.pubKeyPath,
                btcPreGenCredentials.btcSessionCreatorCredentials.privKeyPath
            ).get()
        )

    private val btcAddressSetterCredentials =
        IrohaCredential(
            btcPreGenCredentials.btcAddressSetterCredentials.accountId, ModelUtil.loadKeypair(
                btcPreGenCredentials.btcAddressSetterCredentials.pubKeyPath,
                btcPreGenCredentials.btcAddressSetterCredentials.privKeyPath
            ).get()
        )

    private val notaryPeerListProvider = NotaryPeerListProviderImpl(
        irohaNetwork,
        queryCreatorCredentials,
        btcPreGenConfig.notaryListStorageAccount,
        btcPreGenConfig.notaryListSetterAccount
    )
    private val btcPublicKeyProvider =
        BtcPublicKeyProvider(
            wallet,
            walletFile,
            btcPreGenConfig.iroha,
            btcSessionCreatorCredentials,
            btcAddressSetterCredentials,
            btcPreGenConfig.btcAddressStorageAccount,
            btcPreGenConfig.sessionsDomain,
            notaryPeerListProvider
        )


    /*
    Initiates listener that listens to events in trigger account.
    If trigger account is triggered, new session account full notary public keys will be created
     */
    fun init(): Result<Unit, Exception> {
        return IrohaChainListener(
            btcPreGenConfig.iroha,
            queryCreatorCredentials
        ).getBlockObservable().map { irohaObservable ->
            initIrohaObservable(irohaObservable)
        }
    }

    private fun initIrohaObservable(irohaObservable: Observable<BlockOuterClass.Block>) {
        irohaObservable.subscribe { block ->
            getSetDetailCommands(block).forEach { command ->
                if (command.setAccountDetail.accountId == btcPreGenConfig.pubKeyTriggerAccount) {
                    //add new public key to session account, if trigger account was changed
                    val sessionAccountName = command.setAccountDetail.key
                    onGenerateKey(sessionAccountName).fold(
                        { pubKey -> logger.info { "New public key $pubKey for BTC multisignature address was created" } },
                        { ex -> logger.error("Cannot generate public key for BTC multisignature address", ex) })
                } else if (command.setAccountDetail.accountId.endsWith(btcPreGenConfig.sessionsDomain)) {
                    //create multisignature address, if we have enough keys in session account
                    onGenerateMultiSigAddress(command.setAccountDetail.accountId).failure { ex ->
                        logger.error(
                            "Cannot generate multi signature address", ex
                        )
                    }
                }
            }
        }
    }

    private fun getSetDetailCommands(block: BlockOuterClass.Block): List<Commands.Command> {
        return block.payload.transactionsList.flatMap { tx -> tx.payload.reducedPayload.commandsList }
            .filter { command -> command.hasSetAccountDetail() }
    }

    private fun onGenerateKey(sessionAccountName: String): Result<String, Exception> {
        return btcPublicKeyProvider.createKey(sessionAccountName)
    }

    private fun onGenerateMultiSigAddress(sessionAccount: String): Result<Unit, Exception> {
        return getAccountDetails(
            queryCreatorCredentials,
            irohaNetwork,
            sessionAccount,
            btcAddressSetterCredentials.accountId
        ).flatMap { details ->
            val notaryKeys = details.values
            btcPublicKeyProvider.checkAndCreateMultiSigAddress(notaryKeys)
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()

}
