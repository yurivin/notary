package provider.btc.generation

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import jp.co.soramitsu.iroha.java.IrohaAPI
import model.IrohaCredential
import notary.IrohaCommand
import notary.IrohaTransaction
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.consumer.IrohaConverter
import sidechain.iroha.util.ModelUtil
import util.hex

private const val BTC_SESSION_DOMAIN = "btcSession"
const val ADDRESS_GENERATION_TIME_KEY = "addressGenerationTime"

// Class for creating session accounts. Theses accounts are used to store BTC public keys.
class BtcSessionProvider(
    private val credential: IrohaCredential,
    irohaAPI: IrohaAPI
) {
    private val irohaConsumer = IrohaConsumerImpl(credential, irohaAPI)

    /**
     * Creates a special session account for notaries public key storage
     *
     * @param sessionId session identifier aka session account name
     * @return Result of account creation process
     */
    fun createPubKeyCreationSession(sessionId: String): Result<String, Exception> {
        return Result.of {
            createPubKeyCreationSessionTx(sessionId)
        }.flatMap { irohaTx ->
            val utx = IrohaConverter.convert(irohaTx)
            irohaConsumer.send(utx)
        }
    }

    /**
     * Creates a transaction that may be used to create special session account for notaries public key storage
     *
     * @param sessionId session identifier aka session account name
     * @return Iroha transaction full of session creation commands
     */
    fun createPubKeyCreationSessionTx(sessionId: String): IrohaTransaction {
        return IrohaTransaction(
            credential.accountId,
            ModelUtil.getCurrentTime(),
            1,
            arrayListOf(
                //Creating session account
                IrohaCommand.CommandCreateAccount(
                    sessionId, BTC_SESSION_DOMAIN, String.hex(credential.keyPair.public.encoded)
                ),
                //Setting address generation time
                IrohaCommand.CommandSetAccountDetail(
                    "$sessionId@$BTC_SESSION_DOMAIN",
                    ADDRESS_GENERATION_TIME_KEY,
                    System.currentTimeMillis().toString()
                )
            )
        )
    }
}
