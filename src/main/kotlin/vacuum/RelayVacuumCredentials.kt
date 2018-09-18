package vacuum

import config.IrohaCredentialConfig

/** Credentials of withdrawal service */
interface RelayVacuumCredentials {

    /** Account to make queries list of notaries, list of tokens, list of relay addresses*/
    val notaryQueryCreator: IrohaCredentialConfig
}

