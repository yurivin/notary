package notary.eth

import config.EthereumConfig
import config.IrohaConfig

/** Configuration of refund endpoint in Notary */
interface RefundConfig {
    val port: Int
    val endpointEthereum: String
}

/** Configuration of notary */
interface EthNotaryConfig {
    /** Iroha account for registering wallets */
    val registrationServiceIrohaAccount: String

    /** Iroha account that stores tokens */
    val tokenStorageAccount: String

    /** Iroha account that set whitelist for client */
    val whitelistSetter: String

    /** Iroha account to store notary peer list  */
    val notaryListStorageAccount: String

    /** Iroha account to set notary peer list */
    val notaryListSetterAccount: String

    val refund: RefundConfig

    val iroha: IrohaConfig

    val ethereum: EthereumConfig
}
