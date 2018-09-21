package integration.helper

import config.TestConfig
import config.loadConfigs
import jp.co.soramitsu.iroha.ModelTransactionBuilder
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil
import util.getRandomString

//Class that handles all the accounts in integration tests.
class AccountHelper(private val accountCredential: IrohaCredential) {

    private val testConfig = loadConfigs("test", TestConfig::class.java, "/test.properties")

    private val irohaConsumer = IrohaConsumerImpl(testConfig.iroha, accountCredential)

    /** Account that used to store registered clients.*/
    val registrationAccount by lazy {
        createTesterAccount("registration", "registration_service")
    }

    /** Domain to create new accounts */
    val accountsDomain = "notary"

    val btcSessionsDomain = "btcSessions"

    /** Account that used to store tokens*/
    val tokenStorageAccount by lazy { createTesterAccount("tokens", "token_service") }

    // TODO - D3-348 - dolgopolov.work change to suitable role name
    /** Account that used to store peers*/
    val notaryListSetterAccount by lazy { createTesterAccount("notary_setter", "token_service") }

    val notaryListStorageAccount by lazy { createTesterAccount("notary_storage", "notary_holder") }

    /** Notary account*/
    val storageAccount by lazy { createTesterAccount("notary", "notary") }

    val mstRegistrationAccount by lazy {
        createTesterAccount("mst_registration", "registration_service")
    }


    /**
     * Creates randomly named tester account with the same key in Iroha
     */
    private fun createTesterAccount(prefix: String, roleName: String = "tester"): String {
        val name = prefix + "_${String.getRandomString(9)}"
        val domain = "notary"
        val creator = accountCredential.accountId
        irohaConsumer.sendAndCheck(
            ModelTransactionBuilder()
                .creatorAccountId(creator)
                .createdTime(ModelUtil.getCurrentTime())
                .createAccount(name, domain, accountCredential.keyPair.publicKey())
                .appendRole("$name@$domain", roleName)
                .build()
        ).fold({
            logger.info("account $name@$domain was created")
            return "$name@$domain"
        }, { ex ->
            throw ex
        })
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
