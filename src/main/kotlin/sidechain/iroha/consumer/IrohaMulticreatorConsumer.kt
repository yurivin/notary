package sidechain.iroha.consumer

import com.github.kittinunf.result.Result
import jp.co.soramitsu.iroha.ModelTransactionBuilder
import jp.co.soramitsu.iroha.UnsignedTx
import model.IrohaCredential
import sidechain.iroha.util.ModelUtil

class IrohaMulticreatorConsumer(private val irohaNetwork: IrohaNetwork) {
    private var txs = mutableListOf<Pair<UnsignedTx, IrohaCredential>>()

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

    fun sendAndCheck(): Result<List<String>, Exception> {
        val batch = txs.map { (tx, credential) ->
            ModelUtil.prepareTransaction(tx, credential.keyPair).get()
        }
        val hashes = txs.map { (tx, _) -> tx.hash() }
        return irohaNetwork.sendAndCheck(batch, hashes)
    }
}
