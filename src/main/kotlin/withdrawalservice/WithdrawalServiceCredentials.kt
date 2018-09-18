package withdrawalservice

import config.IrohaCredentialConfig

/** Credentials of withdrawal service */
interface WithdrawalServiceCredentials {

    /** Account to make queries for Iroha blocks */
    val blockQueryCreator: IrohaCredentialConfig

    /** Account to make queries list of notaries, list of tokens, list of relay addresses*/
    val notaryQueryCreator: IrohaCredentialConfig
}

