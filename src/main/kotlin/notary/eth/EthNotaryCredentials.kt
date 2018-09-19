package notary.eth

import config.IrohaCredentialConfig

/** Configuration of notary */
interface EthNotaryCredentials {

    /**
     * Account to form queries to Iroha network
     */
    val queryCreatorCredentials: IrohaCredentialConfig

    /**
     * Notary account credentials
     */
    val notaryCredentials: IrohaCredentialConfig

}