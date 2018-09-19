package notary.btc

import config.IrohaCredentialConfig

interface BtcNotaryCredentials {
    /**
     * Query account to make queries notary list, block query and session account
     */
    val queryCreatorCredentials: IrohaCredentialConfig


    /** Main notary account */
    val notaryCredentials: IrohaCredentialConfig

}
