package registration.eth

import config.IrohaConfig

/**
 * Interface represents configs for registration service for cfg4k
 */
interface EthRegistrationConfig {
    /** Port of registration service */
    val port: Int

    /** Iroha account of relay registration service */
    val relayRegistrationIrohaAccount: String

    /** Iroha account for mapping relay addresses and Iroha accounts */
    val mappingAccount: String

    /** Iroha configuration */
    val iroha: IrohaConfig
}
