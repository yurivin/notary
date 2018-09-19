package registration.btc.pregen

import config.IrohaConfig

interface BtcPreGenConfig {

    // Domain for btc sessions
    val sessionsDomain: String

    // Account that stores generated btc addresses, ready to use
    val btcAddressStorageAccount: String

    /**
     * Account for triggering.
     * Triggering this account means starting BTC addresses pregeneration
     */
    val pubKeyTriggerAccount: String

    //Iroha config
    val iroha: IrohaConfig

    //Path to BTC wallet file
    val btcWalletFilePath: String

    //Account that stores all registered notaries
    val notaryListStorageAccount: String

    //Account that sets registered notaries
    val notaryListSetterAccount: String
}
