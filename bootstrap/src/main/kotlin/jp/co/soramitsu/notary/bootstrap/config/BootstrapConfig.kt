package jp.co.soramitsu.notary.bootstrap.config

import jp.co.soramitsu.notary.bootstrap.genesis.d3.D3TestGenesisFactory
import jp.co.soramitsu.notary.bootstrap.genesis.GenesisInterface
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class BootstrapConfig {

    @Bean
    fun genesisFactories(): List<GenesisInterface> {
        return listOf(D3TestGenesisFactory())
    }
}

