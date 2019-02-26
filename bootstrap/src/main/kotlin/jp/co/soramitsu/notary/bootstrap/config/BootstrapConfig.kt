package jp.co.soramitsu.notary.bootstrap.config

import jp.co.soramitsu.notary.bootstrap.controller.IrohaController
import jp.co.soramitsu.notary.bootstrap.genesis.D3TestGenesisFactory
import jp.co.soramitsu.notary.bootstrap.genesis.GenesisInterface
import jp.co.soramitsu.notary.bootstrap.service.GenesisBlockService
import jp.co.soramitsu.notary.bootstrap.service.GenesisBlockServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class BootstrapConfig {

    @Bean
    fun getGenensisBlockService(): GenesisBlockService {
        return GenesisBlockServiceImpl()
    }

    @Bean
    fun genesisFactories(): List<GenesisInterface> {
        return listOf(D3TestGenesisFactory())
    }
}

