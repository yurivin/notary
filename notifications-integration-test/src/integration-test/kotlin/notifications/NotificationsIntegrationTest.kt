package notifications

import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import integration.helper.IrohaIntegrationHelperUtil
import model.D3_CLIENT_EMAIL_KEY
import model.D3_CLIENT_ENABLE_NOTIFICATIONS
import notifications.environment.NotificationsIntegrationTestEnvironment
import notifications.service.D3_DEPOSIT_EMAIL_SUBJECT
import notifications.service.D3_WITHDRAWAL_EMAIL_SUBJECT
import notifications.service.NOTIFICATION_EMAIL
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import sidechain.iroha.CLIENT_DOMAIN
import sidechain.iroha.util.ModelUtil
import java.math.BigDecimal

const val BTC_ASSET = "btc#bitcoin"
private const val TRANSFER_WAIT_TIME = 3_000L
private const val SRC_USER_EMAIL = "src.user@d3.com"
private const val DEST_USER_EMAIL = "dest.user@d3.com"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationsIntegrationTest {

    private val integrationHelper = IrohaIntegrationHelperUtil()

    private val environment = NotificationsIntegrationTestEnvironment(integrationHelper)

    init {
        // This amount is big enough for testing
        val amount = BigDecimal(100)

        // Creating 2 clients
        integrationHelper.createAccount(environment.srcClientName, CLIENT_DOMAIN, environment.srcClientKeyPair.public)
        integrationHelper.createAccount(environment.destClientName, CLIENT_DOMAIN, environment.destClientKeyPair.public)

        // Setting src client email
        ModelUtil.setAccountDetail(
            environment.srcClientConsumer,
            environment.srcClientId,
            D3_CLIENT_EMAIL_KEY,
            SRC_USER_EMAIL
        ).failure { ex -> throw ex }

        // Setting dest client email
        ModelUtil.setAccountDetail(
            environment.destClientConsumer,
            environment.destClientId,
            D3_CLIENT_EMAIL_KEY,
            DEST_USER_EMAIL
        ).failure { ex -> throw ex }

        // Enriching notary account
        integrationHelper.addIrohaAssetTo(
            integrationHelper.accountHelper.notaryAccount.accountId,
            BTC_ASSET,
            amount
        )

        // Enriching src account
        integrationHelper.addIrohaAssetTo(
            environment.srcClientId,
            BTC_ASSET,
            amount
        )

        // Run notifications service
        environment.notificationInitialization.init()
    }

    @BeforeEach
    fun resetDumbster() {
        environment.dumbster.reset()
    }

    @AfterAll
    fun closeEnvironments() {
        environment.close()
    }

    /**
     * Note: Iroha must be deployed to pass the test.
     * @given D3 client with enabled email notifications and notary account
     * @when notary sends money to D3 client
     * @then D3 client is notified about deposit
     */
    @Test
    fun testNotificationDeposit() {
        val depositValue = BigDecimal(1)

        ModelUtil.setAccountDetail(
            environment.srcClientConsumer,
            environment.srcClientId,
            D3_CLIENT_ENABLE_NOTIFICATIONS,
            "true"
        ).map {
            val notaryAccount = integrationHelper.accountHelper.notaryAccount
            integrationHelper.transferAssetIrohaFromClient(
                notaryAccount.accountId,
                notaryAccount.keyPair,
                notaryAccount.accountId,
                environment.srcClientId,
                BTC_ASSET,
                "no description",
                depositValue.toPlainString()
            )
        }.map {
            Thread.sleep(TRANSFER_WAIT_TIME)
            val receivedEmails = environment.dumbster.receivedEmails
            assertEquals(1, receivedEmails.size)
            val lastEmail = receivedEmails.last()
            assertEquals(D3_DEPOSIT_EMAIL_SUBJECT, lastEmail.getHeaderValue("Subject"))
            assertEquals(SRC_USER_EMAIL, lastEmail.getHeaderValue("To"))
            assertEquals(NOTIFICATION_EMAIL, lastEmail.getHeaderValue("From"))
            Unit
        }.failure { ex -> fail(ex) }
    }

    /**
     * Note: Iroha must be deployed to pass the test.
     * @given D3 client with enabled email notifications and notary account
     * @when D3 client sends money to notary account
     * @then D3 client is notified about withdrawal
     */
    @Test
    fun testNotificationWithdrawal() {
        val withdrawalValue = BigDecimal(1)

        ModelUtil.setAccountDetail(
            environment.srcClientConsumer,
            environment.srcClientId,
            D3_CLIENT_ENABLE_NOTIFICATIONS,
            "true"
        ).map {
            val notaryAccount = integrationHelper.accountHelper.notaryAccount
            integrationHelper.transferAssetIrohaFromClient(
                environment.srcClientId,
                environment.srcClientKeyPair,
                environment.srcClientId,
                notaryAccount.accountId,
                BTC_ASSET,
                "no description",
                withdrawalValue.toPlainString()
            )
        }.map {
            Thread.sleep(TRANSFER_WAIT_TIME)
            val receivedEmails = environment.dumbster.receivedEmails
            assertEquals(1, receivedEmails.size)
            val lastEmail = receivedEmails.last()
            assertEquals(D3_WITHDRAWAL_EMAIL_SUBJECT, lastEmail.getHeaderValue("Subject"))
            assertEquals(SRC_USER_EMAIL, lastEmail.getHeaderValue("To"))
            assertEquals(NOTIFICATION_EMAIL, lastEmail.getHeaderValue("From"))
            Unit
        }.failure { ex -> fail(ex) }
    }

    /**
     * Note: Iroha must be deployed to pass the test.
     * @given 2 D3 clients with enabled email notifications
     * @when 1st client sends money to 2nd
     * @then None of clients are notified, since it's a simple transfer
     */
    @Test
    fun testNotificationSimpleTransfer() {
        val transferValue = BigDecimal(1)

        ModelUtil.setAccountDetail(
            environment.srcClientConsumer,
            environment.srcClientId,
            D3_CLIENT_ENABLE_NOTIFICATIONS,
            "true"
        ).map {
            ModelUtil.setAccountDetail(
                environment.destClientConsumer,
                environment.destClientId,
                D3_CLIENT_ENABLE_NOTIFICATIONS,
                "true"
            )
        }.map {
            integrationHelper.transferAssetIrohaFromClient(
                environment.srcClientId,
                environment.srcClientKeyPair,
                environment.srcClientId,
                environment.destClientId,
                BTC_ASSET,
                "no description",
                transferValue.toPlainString()
            )
        }.map {
            Thread.sleep(TRANSFER_WAIT_TIME)
            assertTrue(environment.dumbster.receivedEmails.isEmpty())
            Unit
        }.failure { ex -> fail(ex) }
    }

    /**
     * Note: Iroha must be deployed to pass the test.
     * @given D3 client with disabled email notifications and notary account
     * @when notary sends money to D3 client
     * @then D3 client is not notified about deposit
     */
    @Test
    fun testNotificationDepositNotEnabled() {
        val depositValue = BigDecimal(1)

        ModelUtil.setAccountDetail(
            environment.srcClientConsumer,
            environment.srcClientId,
            D3_CLIENT_ENABLE_NOTIFICATIONS,
            "false"
        ).map {
            val notaryAccount = integrationHelper.accountHelper.notaryAccount
            integrationHelper.transferAssetIrohaFromClient(
                notaryAccount.accountId,
                notaryAccount.keyPair,
                notaryAccount.accountId,
                environment.srcClientId,
                BTC_ASSET,
                "no description",
                depositValue.toPlainString()
            )
        }.map {
            Thread.sleep(TRANSFER_WAIT_TIME)
            assertTrue(environment.dumbster.receivedEmails.isEmpty())
            Unit
        }.failure { ex -> fail(ex) }
    }
}
