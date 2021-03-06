package notifications.config

import config.IrohaConfig
import config.IrohaCredentialConfig

/**
 * Configuration of notification service
 */
interface NotificationsConfig {
    // Iroha configs
    val iroha: IrohaConfig
    // Path to file with SMTP configs
    val smtpConfigPath: String
    // Notary account credential. Used to listen to Iroha blocks
    val notaryCredential: IrohaCredentialConfig
}

/**
 * SMTP configuration
 */
interface SMTPConfig {
    // SMTP host
    val host: String
    // SMTP port
    val port: Int
    // SMTP user accountId
    val userName: String
    // SMTP password
    val password: String
}
