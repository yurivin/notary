package notary

import config.DatabaseConfig
import config.EthereumConfig
import config.IrohaConfig

/** Configuration of refund endpoint in Notary */
interface RefundConfig {
    val port: Int
    val endpointEthereum: String
}

/** Configuration of notary */
interface NotaryConfig {
    /** Iroha account that has registered wallets */
    val registrationServiceIrohaAccount: String
    val refund: RefundConfig
    val iroha: IrohaConfig
    val ethereum: EthereumConfig
    val db: DatabaseConfig
}