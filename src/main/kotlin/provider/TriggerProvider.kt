package provider

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import config.IrohaConfig
import model.IrohaCredential
import mu.KLogging
import provider.eth.EthRelayProviderIrohaImpl
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil


/**
 * Provider that helps us to implement pub/sub mechanism in Iroha using account as an event source.
 * @param irohaConfig
 * @param triggerAccount - account in Iroha that emulates trigger. Will store some details
 * @param callerAccount - creator of a trigger
 */
class TriggerProvider(
    irohaConfig: IrohaConfig,
    private val callerCredential: IrohaCredential,
    private val triggerAccount: String
) {
    init {
        logger.info {
            "Init trigger provider with caller account '${callerCredential.accountId}' and trigger account '$triggerAccount'"
        }
    }

    private val irohaConsumer = IrohaConsumerImpl(irohaConfig, callerCredential)

    /**
     * Sets payload details to trigger
     *
     * @param payload - some data to store
     * @return Result of detail setting process
     */
    fun trigger(payload: String): Result<Unit, Exception> {
        return ModelUtil.setAccountDetail(
            irohaConsumer,
            callerCredential.accountId,
            triggerAccount,
            payload,
            ""
        ).map {
            logger.info { "$triggerAccount was triggered with payload $payload" }
            Unit
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
