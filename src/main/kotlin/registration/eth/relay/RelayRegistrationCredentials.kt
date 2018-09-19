package registration.eth.relay

import config.IrohaCredentialConfig

/**
 * Interface represents configs for relay registration service for cfg4k
 */
interface RelayRegistrationCredentials {

    /** Account to create and set new relay */
    val relaySetterCredential: IrohaCredentialConfig
}
