package registration.btc

import config.IrohaCredentialConfig


interface BtcRegistrationCredentials {
     /**
     * Query account to make queries notary list, block query and session account
     */
    val queryCreatorCredentials: IrohaCredentialConfig
}
