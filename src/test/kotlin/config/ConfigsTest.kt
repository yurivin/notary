package config

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables


class ConfigsTest {

    @Rule
    @JvmField
    //For environment variables dependent code testing
    val environmentVariables = EnvironmentVariables()

    /**
     * @given command line array full of passwords
     * @when command line is passed to loadEthPasswords()
     * @then EthPasswordsConfig is constructed based on command line
     */
    @Test
    fun testLoadEthPasswordsArgs() {
        val args = arrayOf("argCredentialsPassword", "argNodeLogin", "argNodePassword")
        val ethPasswords = loadEthPasswords("test", "/test.properties", args)
        assertEquals(args[0], ethPasswords.credentialsPassword)
        assertEquals(args[1], ethPasswords.nodeLogin)
        assertEquals(args[2], ethPasswords.nodePassword)
    }

    /**
     * @given command line array and environment variables full of passwords
     * @when command line is passed to loadEthPasswords()
     * @then EthPasswordsConfig is constructed based on command line
     */
    @Test
    fun testLoadEthPasswordsArgsWithEnvVariables() {
        setEnvVariables()
        val args = arrayOf("argCredentialsPassword", "argNodeLogin", "argNodePassword")
        val ethPasswords = loadEthPasswords("test", "/test.properties", args)
        assertEquals(args[0], ethPasswords.credentialsPassword)
        assertEquals(args[1], ethPasswords.nodeLogin)
        assertEquals(args[2], ethPasswords.nodePassword)
    }

    /**
     * @given environment variables full of passwords
     * @when properties file is passed to loadEthPasswords()
     * @then EthPasswordsConfig is constructed based on environment variables
     */
    @Test
    fun testLoadEthPasswordsEnv() {
        setEnvVariables()
        val envCredentialsPassword = System.getenv(ETH_CREDENTIALS_PASSWORD_ENV)
        val envNodeLogin = System.getenv(ETH_NODE_LOGIN_ENV)
        val envNodePassword = System.getenv(ETH_NODE_PASSWORD_ENV)
        val ethPasswords = loadEthPasswords("test", "/test.properties")
        assertEquals(envCredentialsPassword, ethPasswords.credentialsPassword)
        assertEquals(envNodeLogin, ethPasswords.nodeLogin)
        assertEquals(envNodePassword, ethPasswords.nodePassword)
    }

    /**
     * @given properties file
     * @when properties file is passed to loadEthPasswords()
     * @then EthPasswordsConfig is constructed based on properties file
     */
    @Test
    fun testLoadEthPasswordsProperties() {
        val testConfig = loadConfigs("test", TestConfig::class.java, "/test.properties")
        val ethPasswords = loadEthPasswords("test", "/test.properties")
        assertEquals(testConfig.credentialsPassword, ethPasswords.credentialsPassword)
        assertEquals(testConfig.nodeLogin, ethPasswords.nodeLogin)
        assertEquals(testConfig.nodePassword, ethPasswords.nodePassword)
    }

    private fun setEnvVariables() {
        environmentVariables.set(ETH_CREDENTIALS_PASSWORD_ENV, "env_credentialsPassword")
        environmentVariables.set(ETH_NODE_LOGIN_ENV, "env_nodeLogin")
        environmentVariables.set(ETH_NODE_PASSWORD_ENV, "env_nodePassword")
    }
}