package registration.btc

import config.IrohaConfig

/**
 * Interface represents configs for registration service for cfg4k
 */
interface BtcRegistrationConfig {
    /** Port of registration service */
    val port: Int


    val btcAddressStorageAccount: String

    val btcAddressSetterAccount: String

    val btcRegisteredAddressStorageAccount: String

    val btcRegisteredAddressSetterAccount: String



    /** Iroha configuration */
    val iroha: IrohaConfig

    val btcWalletPath: String
}
