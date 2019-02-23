package com.d3.btc.dwbridge.config

import com.d3.btc.fee.BtcFeeRateService
import com.d3.btc.provider.BtcRegisteredAddressesProvider
import config.BitcoinConfig
import config.loadConfigs
import io.grpc.ManagedChannelBuilder
import jp.co.soramitsu.iroha.java.IrohaAPI
import jp.co.soramitsu.iroha.java.QueryAPI
import model.IrohaCredential
import com.d3.btc.deposit.config.BtcDepositConfig
import org.bitcoinj.wallet.Wallet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import provider.NotaryPeerListProviderImpl
import sidechain.iroha.IrohaChainListener
import sidechain.iroha.consumer.IrohaConsumerImpl
import sidechain.iroha.util.ModelUtil
import com.d3.btc.withdrawal.config.BtcWithdrawalConfig
import com.d3.btc.withdrawal.provider.BtcChangeAddressProvider
import com.d3.btc.withdrawal.provider.BtcWhiteListProvider
import com.d3.btc.withdrawal.statistics.WithdrawalStatistics
import java.io.File
import java.util.concurrent.Executors

val withdrawalConfig =
    loadConfigs("btc-withdrawal", BtcWithdrawalConfig::class.java, "/btc/withdrawal.properties").get()
val depositConfig = loadConfigs("btc-deposit", BtcDepositConfig::class.java, "/btc/deposit.properties").get()
val dwBridgeConfig = loadConfigs("btc-dw-bridge", BtcDWBridgeConfig::class.java, "/btc/dw-bridge.properties").get()

@Configuration
class BtcDWBridgeAppConfiguration {

    private val withdrawalKeypair = ModelUtil.loadKeypair(
        withdrawalConfig.withdrawalCredential.pubkeyPath,
        withdrawalConfig.withdrawalCredential.privkeyPath
    ).fold({ keypair -> keypair }, { ex -> throw ex })

    private val notaryKeypair = ModelUtil.loadKeypair(
        depositConfig.notaryCredential.pubkeyPath,
        depositConfig.notaryCredential.privkeyPath
    ).fold({ keypair -> keypair }, { ex -> throw ex })

    private val signatureCollectorKeypair = ModelUtil.loadKeypair(
        withdrawalConfig.signatureCollectorCredential.pubkeyPath,
        withdrawalConfig.signatureCollectorCredential.privkeyPath
    ).fold({ keypair -> keypair }, { ex -> throw ex })

    private val btcFeeRateCredential = ModelUtil.loadKeypair(
        withdrawalConfig.btcFeeRateCredential.pubkeyPath,
        withdrawalConfig.btcFeeRateCredential.privkeyPath
    ).fold({ keypair ->
        IrohaCredential(withdrawalConfig.btcFeeRateCredential.accountId, keypair)
    }, { ex -> throw ex })

    private val notaryCredential =
        IrohaCredential(depositConfig.notaryCredential.accountId, notaryKeypair)

    @Bean
    fun notaryConfig() = depositConfig

    @Bean
    fun irohaAPI(): IrohaAPI {
        val irohaAPI = IrohaAPI(dwBridgeConfig.iroha.hostname, dwBridgeConfig.iroha.port)
        /**
         * It's essential to handle blocks in this service one-by-one.
         * This is why we explicitly set single threaded executor.
         */
        irohaAPI.setChannelForStreamingQueryStub(
            ManagedChannelBuilder.forAddress(
                dwBridgeConfig.iroha.hostname,
                dwBridgeConfig.iroha.port
            ).executor(Executors.newSingleThreadExecutor()).usePlaintext().build()
        )
        return irohaAPI
    }

    @Bean
    fun btcRegisteredAddressesProvider(): BtcRegisteredAddressesProvider {
        ModelUtil.loadKeypair(
            depositConfig.notaryCredential.pubkeyPath,
            depositConfig.notaryCredential.privkeyPath
        )
            .fold({ keypair ->
                return BtcRegisteredAddressesProvider(
                    QueryAPI(irohaAPI(), depositConfig.notaryCredential.accountId, keypair),
                    depositConfig.registrationAccount,
                    depositConfig.notaryCredential.accountId
                )
            }, { ex -> throw ex })
    }

    @Bean
    fun signatureCollectorCredential() =
        IrohaCredential(withdrawalConfig.signatureCollectorCredential.accountId, signatureCollectorKeypair)

    @Bean
    fun signatureCollectorConsumer() = IrohaConsumerImpl(signatureCollectorCredential(), irohaAPI())

    @Bean
    fun wallet() = Wallet.loadFromFile(File(dwBridgeConfig.bitcoin.walletPath))

    @Bean
    fun notaryCredential() = notaryCredential

    @Bean
    fun withdrawalStatistics() = WithdrawalStatistics.create()

    @Bean
    fun withdrawalCredential() =
        IrohaCredential(withdrawalConfig.withdrawalCredential.accountId, withdrawalKeypair)

    @Bean
    fun withdrawalConsumer() = IrohaConsumerImpl(withdrawalCredential(), irohaAPI())

    @Bean
    fun withdrawalConfig() = withdrawalConfig

    @Bean
    fun depositIrohaChainListener() = IrohaChainListener(
        dwBridgeConfig.iroha.hostname,
        dwBridgeConfig.iroha.port,
        notaryCredential()
    )

    @Bean
    fun withdrawalIrohaChainListener() = IrohaChainListener(
        irohaAPI(),
        withdrawalCredential()
    )

    @Bean
    fun withdrawalQueryAPI() = QueryAPI(irohaAPI(), withdrawalCredential().accountId, withdrawalCredential().keyPair)

    @Bean
    fun whiteListProvider(): BtcWhiteListProvider {
        return BtcWhiteListProvider(
            withdrawalConfig.registrationCredential.accountId,
            withdrawalQueryAPI()
        )
    }

    @Bean
    fun btcChangeAddressProvider(): BtcChangeAddressProvider {
        return BtcChangeAddressProvider(
            withdrawalQueryAPI(),
            withdrawalConfig.mstRegistrationAccount,
            withdrawalConfig.changeAddressesStorageAccount
        )
    }

    @Bean
    fun blockStoragePath() = dwBridgeConfig.bitcoin.blockStoragePath

    @Bean
    fun btcHosts() = BitcoinConfig.extractHosts(dwBridgeConfig.bitcoin)

    @Bean
    fun btcFeeRateAccount() = withdrawalConfig.btcFeeRateCredential.accountId

    @Bean
    fun btcFeeRateService() =
        BtcFeeRateService(
            IrohaConsumerImpl(
                btcFeeRateCredential, irohaAPI()
            ),
            btcFeeRateCredential.accountId,
            QueryAPI(irohaAPI(), btcFeeRateCredential.accountId, btcFeeRateCredential.keyPair)
        )

    @Bean
    fun notaryPeerListProvider() =
        NotaryPeerListProviderImpl(
            withdrawalQueryAPI(),
            com.d3.btc.withdrawal.config.withdrawalConfig.notaryListStorageAccount,
            com.d3.btc.withdrawal.config.withdrawalConfig.notaryListSetterAccount
        )
}