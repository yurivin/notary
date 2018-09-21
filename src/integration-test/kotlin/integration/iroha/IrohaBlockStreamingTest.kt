package integration.iroha

import com.github.kittinunf.result.map
import config.TestConfig
import config.TestCredentials
import config.loadConfigs
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import model.IrohaCredential
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import sidechain.iroha.IrohaChainListener
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil
import sidechain.iroha.util.ModelUtil.getCurrentTime
import sidechain.iroha.util.ModelUtil.getModelTransactionBuilder
import java.util.concurrent.TimeUnit

/**
 * Note: Requires Iroha is running.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IrohaBlockStreamingTest {

    /** Test configurations */
    val testConfig = loadConfigs("test", TestConfig::class.java, "/test.properties")
    private val testCredentialsConfig =
        loadConfigs("test", TestCredentials::class.java, "/test_credentials.properties")
    private val testCredential = IrohaCredential(
        testCredentialsConfig.accountCredentials.accountId, ModelUtil.loadKeypair(
            testCredentialsConfig.accountCredentials.pubKeyPath,
            testCredentialsConfig.accountCredentials.privKeyPath
        ).get()
    )

    val creator = testCredential.accountId

    @BeforeAll
    fun setUp() {
        System.loadLibrary("irohajava")
    }

    /**
     * @given Iroha running
     * @when new tx is sent to Iroha
     * @then block arrived to IrohaListener
     */
    @Test
    fun irohaStreamingTest() {
        var cmds = listOf<iroha.protocol.Commands.Command>()

        IrohaChainListener(
            testConfig.iroha,
            testCredential
        ).getBlockObservable()
            .map { obs ->
                obs.map { block ->
                    cmds = block.payload.transactionsList
                        .flatMap {
                            it.payload.reducedPayload.commandsList
                        }
                }.subscribeOn(Schedulers.io()).subscribe()
            }

        val utx = getModelTransactionBuilder()
            .creatorAccountId(creator)
            .createdTime(getCurrentTime())
            .setAccountDetail(creator, "test", "test")
            .build()

        IrohaConsumerImpl(testConfig.iroha, testCredential).sendAndCheck(utx)
        runBlocking {
            delay(5000, TimeUnit.MILLISECONDS)
        }

        assertEquals(1, cmds.size)
        assertEquals(creator, cmds.first().setAccountDetail.accountId)
        assertEquals("test", cmds.first().setAccountDetail.key)
        assertEquals("test", cmds.first().setAccountDetail.value)
    }

    /**
     * @given Iroha running
     * @when new tx is sent to Iroha
     * @then block arrived to IrohaListener and returned as coroutine
     */
    @Test
    fun irohaGetBlockTest() {
        val listener = IrohaChainListener(
            testConfig.iroha,
            testCredential
        )

        val block = async {
            listener.getBlock()
        }

        val utx = getModelTransactionBuilder()
            .creatorAccountId(creator)
            .createdTime(getCurrentTime())
            .setAccountDetail(creator, "test", "test")
            .build()

        IrohaConsumerImpl(testConfig.iroha, testCredential).sendAndCheck(utx)

        runBlocking {
            val bl = block.await()
            val cmds = bl.payload.transactionsList
                .flatMap {
                    it.payload.reducedPayload.commandsList
                }
            assertEquals(1, cmds.size)
            assertEquals(creator, cmds.first().setAccountDetail.accountId)
            assertEquals("test", cmds.first().setAccountDetail.key)
            assertEquals("test", cmds.first().setAccountDetail.value)
        }
    }
}
