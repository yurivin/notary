package notary

import com.github.kittinunf.result.Result
import config.IrohaConfig
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import model.IrohaCredential
import mu.KLogging
import provider.NotaryPeerListProviderImpl
import sidechain.SideChainEvent
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.consumer.IrohaConverterImpl
import sidechain.iroha.consumer.IrohaNetworkImpl
import java.math.BigInteger
import java.util.concurrent.Executors

/**
 * Implementation of [Notary] business logic
 * @param irohaConfig
 * @param primaryChainEvents - observable on sidechain events
 * @param domain -
 * @param notaryListSetterAccount - new notary creator
 * @param notaryListStorageAccount - account with a list of notaries
 */
class NotaryImpl(
    private val irohaConfig: IrohaConfig,
    private val primaryChainEvents: Observable<SideChainEvent.PrimaryBlockChainEvent>,
    private val domain: String,
    queryCreator: IrohaCredential,
    private val notaryAccount: IrohaCredential,
    notaryListStorageAccount: String,
    notaryListSetterAccount: String
) : Notary {

    private val irohaNetwork = IrohaNetworkImpl(irohaConfig.hostname, irohaConfig.port)

    private val peerListProvider = NotaryPeerListProviderImpl(
        irohaNetwork,
        queryCreator,
        notaryListStorageAccount,
        notaryListSetterAccount
    )


    /**
     * Handles primary chain deposit event. Notaries create the ordered bunch of
     * transactions: {tx1: setAccountDetail, tx2: addAssetQuantity, transferAsset}.
     * SetAccountDetail insert into notary account information about the transaction (hash) for rollback.
     */
    private fun onPrimaryChainDeposit(
        hash: String,
        time: BigInteger,
        account: String,
        asset: String,
        amount: String,
        from: String
    ): IrohaOrderedBatch {

        logger.info { "Transfer $asset event: hash($hash) time($time) user($account) asset($asset) value ($amount)" }

        val quorum = peerListProvider.getPeerList().size

        return IrohaOrderedBatch(
            arrayListOf(
                IrohaTransaction(
                    notaryAccount.accountId,
                    time,
                    quorum,
                    arrayListOf(
                        // insert into Iroha account information for rollback
                        IrohaCommand.CommandSetAccountDetail(
                            notaryAccount.accountId,
                            "last_tx",
                            hash
                        )
                    )
                ),
                IrohaTransaction(
                    notaryAccount.accountId,
                    time,
                    quorum,
                    arrayListOf(
                        IrohaCommand.CommandAddAssetQuantity(
                            "$asset#$domain",
                            amount
                        ),
                        IrohaCommand.CommandTransferAsset(
                            notaryAccount.accountId,
                            account,
                            "$asset#$domain",
                            from,
                            amount
                        )
                    )
                )
            )
        )
    }

    /**
     * Handle primary chain event
     */
    override fun onPrimaryChainEvent(chainInputEvent: SideChainEvent.PrimaryBlockChainEvent): IrohaOrderedBatch {
        logger.info { "Notary performs primary chain event $chainInputEvent" }
        return when (chainInputEvent) {
            is SideChainEvent.PrimaryBlockChainEvent.OnPrimaryChainDeposit -> onPrimaryChainDeposit(
                chainInputEvent.hash,
                chainInputEvent.time,
                chainInputEvent.user,
                chainInputEvent.asset,
                chainInputEvent.amount,
                chainInputEvent.from
            )
        }
    }

    /**
     * Relay side chain [SideChainEvent] to Iroha output
     */
    override fun irohaOutput(): Observable<IrohaOrderedBatch> {
        return primaryChainEvents.map { event ->
            onPrimaryChainEvent(event)
        }
    }

    /**
     * Init Iroha consumer
     */
    override fun initIrohaConsumer(): Result<Unit, Exception> {
        logger.info { "Init Iroha consumer" }
        val irohaConsumer = IrohaConsumerImpl(irohaConfig, notaryAccount)

        // Init Iroha Consumer pipeline
        // convert from Notary model to Iroha model
        irohaOutput()
            .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
            .subscribe(
                // send to Iroha network layer
                { batch ->
                    val lst = IrohaConverterImpl().convert(batch)
                    irohaConsumer.sendAndCheck(lst)
                        .fold(
                            { logger.info { "Send to Iroha success" } },
                            { ex -> logger.error("Send failure", ex) }
                        )
                },
                // on error
                { ex -> logger.error("OnError called", ex) },
                // should be never called
                { logger.error { "OnComplete called" } }
            )
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
