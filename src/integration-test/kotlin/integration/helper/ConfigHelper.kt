package integration.helper

import config.*
import notary.btc.BtcNotaryConfig
import notary.btc.BtcNotaryCredentials
import notary.eth.EthNotaryConfig
import notary.eth.EthNotaryCredentials
import notary.eth.RefundConfig
import registration.btc.BtcRegistrationConfig
import registration.btc.BtcRegistrationCredentials
import registration.btc.pregen.BtcPreGenConfig
import registration.btc.pregen.BtcPreGenCredentials
import registration.eth.EthRegistrationConfig
import registration.eth.EthRegistrationCredentials
import registration.eth.relay.RelayRegistrationConfig
import registration.eth.relay.RelayRegistrationCredentials
import token.ERC20TokenRegistrationConfig
import token.ERC20TokenRegistrationCredentials
import vacuum.RelayVacuumConfig
import vacuum.RelayVacuumCredentials
import withdrawalservice.WithdrawalServiceConfig
import withdrawalservice.WithdrawalServiceCredentials
import java.util.concurrent.atomic.AtomicInteger

//Class that handles all the configuration objects.
class ConfigHelper(private val accountHelper: AccountHelper) {

    /** Notary credentials **/
    val ethNotaryCredentials =
        loadConfigs("eth-notary", EthNotaryCredentials::class.java, "/eth/notary_credentials.properties")

    val relayRegistrationCredentials =
        loadConfigs(
            "relay-registration",
            RelayRegistrationCredentials::class.java,
            "/eth/relay_registration_credentials.properties"
        )

    /** Configurations for tests */
    val testConfig = loadConfigs("test", TestConfig::class.java, "/test.properties")

    /** Ethereum password configs */
    val ethPasswordConfig = loadEthPasswords("test", "/eth/ethereum_password.properties")

    /** Configuration for notary instance */
    val ethNotaryConfig = loadConfigs("eth-notary", EthNotaryConfig::class.java, "/eth/notary.properties")

    /** Configuration for withdrawal service instance */
    val withdrawalConfig =
        loadConfigs("withdrawal", WithdrawalServiceConfig::class.java, "/eth/withdrawal.properties")

    /** Configuration for registration instance */
    val ethRegistrationConfig =
        loadConfigs("eth-registration", EthRegistrationConfig::class.java, "/eth/registration.properties")

    val btcNotaryConfig = loadConfigs("btc-notary", BtcNotaryConfig::class.java, "/btc/notary.properties")

    val relayRegistrationConfig =
        loadConfigs("test", RelayRegistrationConfig::class.java, "/test.properties")

    val btcRegistrationConfig =
        loadConfigs("btc-registration", BtcRegistrationConfig::class.java, "/btc/registration.properties")

    val btcPkPreGenConfig =
        loadConfigs("btc-pregen", BtcPreGenConfig::class.java, "/btc/pregeneration.properties")

    val erC20TokenRegistrationCredentials = loadConfigs(
        "token-registration",
        ERC20TokenRegistrationCredentials::class.java,
        "/eth/token_registration_credentials.properties"
    )

    val relayVacuumCredentials = loadConfigs(
        "relay-vacuum",
        RelayVacuumCredentials::class.java,
        "/eth/vacuum_credentials.properties"
    )

    val ethRegistrationCredentials =
        loadConfigs(
            "eth-registration",
            EthRegistrationCredentials::class.java,
            "/eth/registration_credentials.properties"
        )

    val ethWithdrawalCredentials =
        loadConfigs("withdrawal", WithdrawalServiceCredentials::class.java, "/eth/withdrawal_credentials.properties")

    val btcNotaryCredentials =
        loadConfigs("btc-notary", BtcNotaryCredentials::class.java, "/btc/notary_credentials.properties")

    val btcPreGenCredentials =
        loadConfigs("btc-pregen", BtcPreGenCredentials::class.java, "/btc/pregeneration_credentials.properties")

    val btcRegistrationCredentials =
        loadConfigs(
            "btc-registration",
            BtcRegistrationCredentials::class.java,
            "/btc/registration_credentials.properties"
        )


    //Creates config for ERC20 tokens registration
    fun createERC20TokenRegistrationConfig(tokensFilePath_: String): ERC20TokenRegistrationConfig {
        return object : ERC20TokenRegistrationConfig {
            override val iroha: IrohaConfig
                get() = createIrohaConfig()
            override val tokensFilePath: String
                get() = tokensFilePath_
            override val tokenStorageAccount: String
                get() = accountHelper.storageAccount
        }
    }

    //Creates config for BTC multisig addresses generation
    fun createBtcPreGenConfig(): BtcPreGenConfig {
        return object : BtcPreGenConfig {
            override val sessionsDomain: String
                get() = accountHelper.btcSessionsDomain
            override val btcAddressStorageAccount: String
                get() = accountHelper.storageAccount
            override val notaryListStorageAccount: String
                get() = accountHelper.notaryListStorageAccount
            override val notaryListSetterAccount: String
                get() = accountHelper.notaryListSetterAccount
            override val pubKeyTriggerAccount: String
                get() = btcPkPreGenConfig.pubKeyTriggerAccount
            override val iroha: IrohaConfig
                get() = createIrohaConfig()
            override val btcWalletFilePath: String
                get() = btcPkPreGenConfig.btcWalletFilePath
        }
    }

    //Creates config for ETH relays registration
    fun createRelayRegistrationConfig(): RelayRegistrationConfig {
        return object : RelayRegistrationConfig {
            override val number: Int
                get() = relayRegistrationConfig.number
            override val ethMasterWallet: String
                get() = relayRegistrationConfig.ethMasterWallet
            override val relayStorageAccount: String
                get() = accountHelper.storageAccount
            override val iroha: IrohaConfig
                get() = createIrohaConfig()
            override val ethereum: EthereumConfig
                get() = relayRegistrationConfig.ethereum
        }
    }

    /** Test configuration for Iroha */
    fun createIrohaConfig(): IrohaConfig {
        return object : IrohaConfig {
            override val hostname: String
                get() = testConfig.iroha.hostname
            override val port: Int
                get() = testConfig.iroha.port
        }
    }

    /**
     * Test configuration for refund endpoint
     * Create unique port for refund for every call
     */
    fun createRefundConfig(): RefundConfig {
        return object : RefundConfig {
            override val endpointEthereum = ethNotaryConfig.refund.endpointEthereum
            override val port = portCounter.incrementAndGet()
        }
    }

    fun createBtcNotaryConfig(): BtcNotaryConfig {
        return object : BtcNotaryConfig {
            override val btcRegisteredAddressStorageAccount: String
                get() = accountHelper.storageAccount
            override val btcRegisteredAddressSetterAccount: String
                get() = accountHelper.mstRegistrationAccount
            override val iroha: IrohaConfig
                get() = createIrohaConfig()
            override val bitcoin: BitcoinConfig
                get() = btcNotaryConfig.bitcoin
            override val notaryListStorageAccount = accountHelper.notaryListStorageAccount
            override val notaryListSetterAccount = accountHelper.notaryListSetterAccount
        }
    }

    fun createBtcRegistrationConfig(): BtcRegistrationConfig {
        return object : BtcRegistrationConfig {
            override val btcRegisteredAddressStorageAccount: String
                get() = accountHelper.mstRegistrationAccount
            override val btcAddressSetterAccount: String
                get() = accountHelper.registrationAccount
            override val btcAddressStorageAccount: String
                get() = accountHelper.storageAccount
            override val port: Int
                get() = btcRegistrationConfig.port
            override val accountsDomain: String
                get() = accountHelper.accountsDomain
            override val iroha: IrohaConfig
                get() = createIrohaConfig()
            override val btcWalletPath: String
                get() = btcRegistrationConfig.btcWalletPath
        }
    }

    /** Test configuration of Notary with runtime dependencies */
    fun createEthNotaryConfig(): EthNotaryConfig {
        return object : EthNotaryConfig {
            override val relaySetterAccount = accountHelper.registrationAccount
            override val relayStorageAccount = accountHelper.storageAccount
            override val tokenStorageAccount = accountHelper.tokenStorageAccount
            override val tokenCreatorAccount = accountHelper.registrationAccount
            override val notaryListStorageAccount = accountHelper.notaryListStorageAccount
            override val notaryListSetterAccount = accountHelper.notaryListSetterAccount
            override val whitelistSetter = testConfig.whitelistSetter
            override val refund = createRefundConfig()
            override val iroha = createIrohaConfig()
            override val ethereum = ethNotaryConfig.ethereum
        }
    }

    /** Test configuration of Withdrawal service with runtime dependencies */
    fun createWithdrawalConfig(
    ): WithdrawalServiceConfig {
        return object : WithdrawalServiceConfig {
            override val withdrawalTriggerAccount = accountHelper.registrationAccount
            override val relayStorageAccount = accountHelper.storageAccount
            override val relaySetterAccount = accountHelper.registrationAccount
            override val tokenCreatorAccount = accountHelper.registrationAccount
            override val tokenStorageAccount = accountHelper.tokenStorageAccount
            override val notaryListStorageAccount = accountHelper.notaryListStorageAccount
            override val notaryListSetterAccount = accountHelper.notaryListSetterAccount
            override val iroha = createIrohaConfig()
            override val ethereum = withdrawalConfig.ethereum
        }
    }

    /** Test configuration of Registration with runtime dependencies */
    fun createEthRegistrationConfig(): EthRegistrationConfig {
        return object : EthRegistrationConfig {
            override val port = portCounter.incrementAndGet()
            override val defaultDomain = accountHelper.accountsDomain
            override val relaySetterAccount = accountHelper.registrationAccount
            override val relayStorageAccount = accountHelper.storageAccount
            override val iroha = createIrohaConfig()
        }
    }

    fun createRelayVacuumConfig(): RelayVacuumConfig {
        return object : RelayVacuumConfig {
            override val tokenCreatorAccount = accountHelper.registrationAccount
            override val tokenStorageAccount = accountHelper.tokenStorageAccount
            override val relaySetterAccount = accountHelper.registrationAccount
            override val relayStorageAccount = accountHelper.storageAccount
            override val iroha = createIrohaConfig()
            override val ethereum = testConfig.ethereum
        }
    }

    companion object {
        /** Port counter, so new port is generated for each run */
        private val portCounter = AtomicInteger(19_999)
    }
}
