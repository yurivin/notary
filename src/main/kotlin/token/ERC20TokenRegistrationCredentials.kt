package token

import config.IrohaCredentialConfig

interface ERC20TokenRegistrationCredentials {
    val tokenCreatorAccount: IrohaCredentialConfig
}
