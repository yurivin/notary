package notary.btc.config

import config.loadConfigs
import model.IrohaCredential
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import provider.btc.address.BtcRegisteredAddressesProvider
import sidechain.iroha.consumer.IrohaNetwork
import sidechain.iroha.consumer.IrohaNetworkImpl
import sidechain.iroha.util.ModelUtil

val notaryConfig = loadConfigs("btc-notary", BtcNotaryConfig::class.java, "/btc/notary.properties")

@Configuration
class BtcNotaryAppConfiguration {

    @Bean
    fun notaryConfig() = notaryConfig

    @Bean
    // TODO add annotation @PreDestroy in order to close()
    fun irohaNetwork() = IrohaNetworkImpl(notaryConfig.iroha.hostname, notaryConfig.iroha.port)

    @Bean
    @Autowired
    fun btcRegisteredAddressesProvider(irohaNetwork: IrohaNetwork): BtcRegisteredAddressesProvider {
        ModelUtil.loadKeypair(notaryConfig.notaryCredential.pubkeyPath, notaryConfig.notaryCredential.privkeyPath)
            .fold({ keypair ->
                return BtcRegisteredAddressesProvider(
                    IrohaCredential(notaryConfig.notaryCredential.accountId, keypair),
                    irohaNetwork,
                    notaryConfig.registrationAccount,
                    notaryConfig.notaryCredential.accountId
                )
            }, { ex -> throw ex })
    }

    @Bean
    fun healthCheckIrohaConfig() = notaryConfig.iroha

    @Bean
    fun irohaHealthCheckCredential(): IrohaCredential {
        //Assuming Iroha library is loaded
        return ModelUtil.loadKeypair(
            notaryConfig.notaryCredential.pubkeyPath,
            notaryConfig.notaryCredential.privkeyPath
        ).fold({ keypair -> IrohaCredential(notaryConfig.notaryCredential.accountId, keypair) }, { ex -> throw ex })
    }
}
