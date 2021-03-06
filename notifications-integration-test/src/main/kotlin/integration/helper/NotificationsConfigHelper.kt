package integration.helper

import notifications.config.NotificationsConfig

class NotificationsConfigHelper(private val accountHelper: IrohaAccountHelper) : IrohaConfigHelper() {

    /**
     * Creates notification services config
     */
    fun createNotificationsConfig(): NotificationsConfig {
        return object : NotificationsConfig {
            override val iroha = createIrohaConfig()
            override val smtpConfigPath = "smtp_test.properties"
            override val notaryCredential = accountHelper.createCredentialConfig(accountHelper.notaryAccount)
        }
    }
}
