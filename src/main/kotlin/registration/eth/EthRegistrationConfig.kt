package registration.eth

import config.IrohaConfig

/**
 * Interface represents configs for registration service for cfg4k
 */
interface EthRegistrationConfig {
    /** Port of registration service */
    val port: Int

    /** Iroha domain for clients with default client role*/
    val defaultDomain: String

    /** Iroha account of relay registration service */
    val relaySetterAccount: String

    /** Iroha account for mapping relay addresses and Iroha accounts */
    val relayStorageAccount: String

    /** Iroha configuration */
    val iroha: IrohaConfig
}
