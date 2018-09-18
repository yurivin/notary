package vacuum

import config.EthereumConfig
import config.IrohaConfig

interface RelayVacuumConfig {

    /** Iroha account that has registered wallets */
    val relayStorageAccount: String

    val relaySetterAccount: String

    /** Iroha account that stores tokens */
    val tokenStorageAccount: String

    /** Account that creates and sets tokens */
    val tokenCreatorAccount: String

    /** Iroha configurations */
    val iroha: IrohaConfig

    /** Ethereum configurations */
    val ethereum: EthereumConfig

}
