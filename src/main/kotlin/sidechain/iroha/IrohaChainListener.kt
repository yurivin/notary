package sidechain.iroha

import com.github.kittinunf.result.Result
import config.IrohaConfig
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import jp.co.soramitsu.iroha.ModelBlocksQueryBuilder
import model.IrohaCredential
import mu.KLogging
import sidechain.ChainListener
import sidechain.iroha.util.ModelUtil
import java.math.BigInteger

/**
 * Dummy implementation of [ChainListener] with effective dependencies
 * @param irohaConfig
 * @param queryCreator
 */
class IrohaChainListener(
    irohaConfig: IrohaConfig,
    queryCreator: IrohaCredential
) : ChainListener<iroha.protocol.BlockOuterClass.Block> {
    val uquery = ModelBlocksQueryBuilder()
        .creatorAccountId(queryCreator.accountId)
        .createdTime(ModelUtil.getCurrentTime())
        .queryCounter(BigInteger.valueOf(1))
        .build()

    val query = ModelUtil.prepareBlocksQuery(uquery, queryCreator.keyPair)
    val stub = ModelUtil.getQueryStub(ModelUtil.getChannel(irohaConfig.hostname, irohaConfig.port))

    /**
     * Returns an observable that emits a new block every time it gets it from Iroha
     */
    override fun getBlockObservable(): Result<Observable<iroha.protocol.BlockOuterClass.Block>, Exception> {
        return Result.of {
            logger.info { "On subscribe to Iroha chain" }
            stub.fetchCommits(query).toObservable().map {
                logger.info { "New Iroha block arrived. Height ${it.blockResponse.block.payload.height}" }
                it.blockResponse.block
            }
        }
    }

    /**
     * @return a block as soon as it is committed to iroha
     */
    override suspend fun getBlock(): iroha.protocol.BlockOuterClass.Block {
        return getBlockObservable().get().blockingFirst()
    }


    /**
     * Logger
     */
    companion object : KLogging()
}
