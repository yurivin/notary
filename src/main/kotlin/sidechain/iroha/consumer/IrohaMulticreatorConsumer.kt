package sidechain.iroha.consumer

import com.github.kittinunf.result.Result
import jp.co.soramitsu.iroha.ModelTransactionBuilder
import jp.co.soramitsu.iroha.UnsignedTx
import model.IrohaCredential
import sidechain.iroha.util.ModelUtil

/**
 * Class to send transactions from different creators in a single batch.
 * The performance is much faster than [IrohaConsumerImpl].
 * Usage:
 *  - add txs to a batch with addTx()
 *  - then send the batch with sendAndCheck()
 */
class IrohaMulticreatorConsumer(private val irohaNetwork: IrohaNetwork) {
    private var txs = mutableListOf<Pair<UnsignedTx, IrohaCredential>>()

    /**
     * Add transaction to the batch to send them later
     */
    fun addTx(tx: ModelTransactionBuilder, credential: IrohaCredential) {
        txs.add(
            Pair(
                tx.quorum(1)
                    .creatorAccountId(credential.accountId)
                    .createdTime(ModelUtil.getCurrentTime())
                    .build(),
                credential
            )
        )
    }

    /**
     * Send a batch of all transactions.
     */
    fun sendAndCheck(): Result<List<String>, Exception> {
        val batch = txs.map { (tx, credential) ->
            ModelUtil.prepareTransaction(tx, credential.keyPair).get()
        }
        val hashes = txs.map { (tx, _) -> tx.hash() }

        txs.clear()

        return irohaNetwork.sendAndCheck(batch, hashes)
    }
}
