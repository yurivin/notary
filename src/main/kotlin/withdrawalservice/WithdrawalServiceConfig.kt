package withdrawalservice

import config.EthereumConfig
import config.IrohaConfig
import config.IrohaCredentialConfig

/** Configuration of withdrawal service */
interface WithdrawalServiceConfig {

    /** Notary account in Iroha */
    val relayStorageAccount: String

    /** Account setter account in Iroha */
    val relaySetterAccount: String

    /** Account a transfer to will trigger withdrawal event **/
    val withdrawalTriggerAccount: String

    /** Iroha account that stores tokens */
    val tokenStorageAccount: String

    /** Token setter in [tokenStorageAccount] */
    val tokenCreatorAccount: String

    /** Notary storage account in Iroha */
    val notaryListStorageAccount: String

    /** Account who sets account details */
    val notaryListSetterAccount: String

    /** Iroha configuration */
    val iroha: IrohaConfig

    /** Ethereum config */
    val ethereum: EthereumConfig
}
