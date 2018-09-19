package registration.eth

import config.IrohaCredentialConfig

/**
 * Interface represents configs for registration service for cfg4k
 */
interface EthRegistrationCredentials {
    val registrationServiceAccount: IrohaCredentialConfig
}
