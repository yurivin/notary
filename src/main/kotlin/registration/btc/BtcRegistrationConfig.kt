package registration.btc

import config.IrohaConfig

/**
 * Interface represents configs for registration service for cfg4k
 */
interface BtcRegistrationConfig {
    /** Port of registration service */
    val port: Int

    /**
     * Account to store all bitcoin addresses
     */
    val btcAddressStorageAccount: String

    /**
     * Session key address setter
     */
    val btcAddressSetterAccount: String

    /**
     * Storage account for new addresses
     */
    val btcRegisteredAddressStorageAccount: String

    /**
     * Default domain for new accounts
     */
    val accountsDomain: String


    /** Iroha configuration */
    val iroha: IrohaConfig

    val btcWalletPath: String
}
