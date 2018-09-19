package registration.btc.pregen

import config.IrohaCredentialConfig
import model.IrohaCredential

interface BtcPreGenCredentials {

    /**
     * Account to put sessions/individual keys
     */
    val btcSessionCreatorCredentials: IrohaCredentialConfig

    /**
     * Account to create/set final multisig Bitcoin addresses
     */
    val btcAddressSetterCredentials: IrohaCredentialConfig

    /**
     * Query account to make queries notary list, block query and session account
     */
    val queryCreatorCredentials: IrohaCredentialConfig

}
