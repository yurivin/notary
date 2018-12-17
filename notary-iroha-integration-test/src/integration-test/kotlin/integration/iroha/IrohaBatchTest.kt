package integration.iroha

import integration.helper.IrohaConfigHelper
import integration.helper.IrohaIntegrationHelperUtil
import jp.co.soramitsu.iroha.Blob
import jp.co.soramitsu.iroha.ModelCrypto
import jp.co.soramitsu.iroha.iroha
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import notary.IrohaCommand
import notary.IrohaOrderedBatch
import notary.IrohaTransaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import sidechain.iroha.CLIENT_DOMAIN
import sidechain.iroha.IrohaChainListener
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.consumer.IrohaConverterImpl
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.ModelUtil
import sidechain.iroha.util.getAccountAsset
import sidechain.iroha.util.getAccountData
import sidechain.iroha.util.toByteVector
import util.getRandomString
import java.time.Duration
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IrohaBatchTest {

    init {
        System.loadLibrary("irohajava")
    }

    private val integrationHelper = IrohaIntegrationHelperUtil()

    val testConfig = integrationHelper.testConfig

    private val testCredential = integrationHelper.testCredential

    private val tester = testCredential.accountId

    val assetDomain = "notary"

    private val irohaNetwork = IrohaNetworkImpl(testConfig.iroha.hostname, testConfig.iroha.port)

    val listener = IrohaChainListener(
        testConfig.iroha.hostname,
        testConfig.iroha.port,
        testCredential
    )

    private val timeoutDuration = Duration.ofMinutes(IrohaConfigHelper.timeoutMinutes)

    private fun randomString() = String.getRandomString(10)

    @AfterAll
    fun dropDown() {
        integrationHelper.close()
        irohaNetwork.close()
        listener.close()
    }

    /**
     * @given A batch
     * @when All transactions are valid and sent as a batch
     * @then They all are accepted and committed in the same block
     */
    @Test
    fun allValidBatchTest() {
        Assertions.assertTimeoutPreemptively(timeoutDuration) {

            val user = randomString()
            val asset_name = randomString()

            val irohaConsumer = IrohaConsumerImpl(testCredential, irohaNetwork)

            val userId = "$user@$CLIENT_DOMAIN"


            val txList =
                listOf(
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandCreateAccount(
                                user,
                                CLIENT_DOMAIN,
                                ModelCrypto().generateKeypair().publicKey().hex()
                            )
                        )
                    ),
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandSetAccountDetail(
                                userId,
                                "key",
                                "value"
                            )
                        )
                    ),
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandCreateAsset(
                                asset_name,
                                assetDomain,
                                0
                            ),
                            IrohaCommand.CommandAddAssetQuantity(
                                "$asset_name#$assetDomain",
                                "100"
                            ),
                            IrohaCommand.CommandTransferAsset(
                                tester,
                                userId,
                                "$asset_name#$assetDomain",
                                "desc",
                                "27"
                            )
                        )
                    )

                )

            val batch = IrohaOrderedBatch(txList)
            val lst = IrohaConverterImpl().convert(batch)
            val hashes = lst.map { it.hash().hex() }

            val blockHashes = GlobalScope.async {
                listener.getBlock().payload.transactionsList.map {
                    Blob(iroha.hashTransaction(it.toByteArray().toByteVector())).hex()
                }
            }

            val successHash = irohaConsumer.sendAndCheck(lst).get()

            val accountJson = getAccountData(testCredential, irohaNetwork, userId).get().toJsonString()
            val tester_amount = getAccountAsset(testCredential, irohaNetwork, tester, "$asset_name#$assetDomain").get()
            val u1_amount =
                getAccountAsset(testCredential, irohaNetwork, userId, "$asset_name#$assetDomain").get()

            assertEquals(hashes, successHash)
            assertEquals("{\"$tester\":{\"key\":\"value\"}}", accountJson)
            assertEquals(73, tester_amount.toInt())
            assertEquals(27, u1_amount.toInt())

            runBlocking {
                withTimeout(10_000) {
                    assertEquals(hashes, blockHashes.await())
                }
            }
        }
    }

    /**
     * @given A batch
     * @when Not all transactions are valid and sent as a batch
     * @then Only valid are accepted and committed in the same block
     */
    @Test
    fun notAllValidBatchTest() {
        Assertions.assertTimeoutPreemptively(timeoutDuration) {

            val user = randomString()
            val asset_name = randomString()

            val irohaConsumer = IrohaConsumerImpl(testCredential, irohaNetwork)

            val userId = "$user@$CLIENT_DOMAIN"
            val assetId = "$asset_name#$assetDomain"

            val txList =
                listOf(
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandCreateAccount(
                                user,
                                CLIENT_DOMAIN,
                                ModelCrypto().generateKeypair().publicKey().hex()
                            )
                        )
                    ),
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandSetAccountDetail(
                                userId,
                                "key",
                                "value"
                            )
                        )
                    ),
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandCreateAsset(
                                asset_name,
                                assetDomain,
                                0
                            ),
                            IrohaCommand.CommandAddAssetQuantity(
                                assetId,
                                "100"
                            ),
                            IrohaCommand.CommandTransferAsset(
                                tester,
                                userId,
                                assetId,
                                "desc",
                                "27"
                            )
                        )
                    ),
                    IrohaTransaction(
                        tester,
                        ModelUtil.getCurrentTime(),
                        1,
                        listOf(
                            IrohaCommand.CommandTransferAsset(
                                tester,
                                userId,
                                assetId,
                                "",
                                "1234"
                            )
                        )
                    )

                )

            val batch = IrohaOrderedBatch(txList)
            val lst = IrohaConverterImpl().convert(batch)
            val hashes = lst.map { it.hash().hex() }
            val expectedHashes = hashes.subList(0, hashes.size - 1)

            val blockHashes = GlobalScope.async {
                listener.getBlock().payload.transactionsList.map {
                    Blob(iroha.hashTransaction(it.toByteArray().toByteVector())).hex()
                }
            }

            val successHash = irohaConsumer.sendAndCheck(lst).get()

            val accountJson = getAccountData(testCredential, irohaNetwork, userId).get().toJsonString()
            val tester_amount = getAccountAsset(testCredential, irohaNetwork, tester, assetId).get()
            val u1_amount =
                getAccountAsset(testCredential, irohaNetwork, userId, assetId).get()

            assertEquals(expectedHashes, successHash)
            assertEquals("{\"$tester\":{\"key\":\"value\"}}", accountJson)
            assertEquals(73, tester_amount.toInt())
            assertEquals(27, u1_amount.toInt())

            runBlocking {
                withTimeout(10_000) {
                    assertEquals(expectedHashes, blockHashes.await())
                }
            }
        }
    }
}