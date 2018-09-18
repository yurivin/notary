package provider.eth

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import config.IrohaConfig
import model.IrohaCredential
import mu.KLogging
import sidechain.iroha.consumer.IrohaConsumerImpl

import sidechain.iroha.util.ModelUtil

/** Interface of an instance that provides with ethereum ERC20 token white list.
 * @param irohaConfig - Iroha configuration
 * @param tokenCreatorAccount - account that is a creator of assets, and credentials
 * @param tokenStorageAccount - tokenStorageAccount that holds tokens in mappingAccount account
 */w
class ERC20TokensCreator(private val irohaConfig: IrohaConfig,
                         private val tokenCreatorAccount: IrohaCredential,
                         private val tokenStorageAccount: String) {


    private val irohaConsumer = IrohaConsumerImpl(irohaConfig, tokenCreatorAccount)

    /**
     * Adds all given ERC20 tokens in Iroha.
     * Creator must have permissions to
     * @param tokens - map of tokens (address->token info)
     * @return Result of operation
     */
    fun addTokens(tokens: Map<String, EthTokenInfo>): Result<Unit, Exception> {
        EthTokensProviderImpl.logger.info { "ERC20 tokens to register $tokens" }

        return ModelUtil.registerERC20Tokens(tokens, tokenCreatorAccount.accountId, tokenStorageAccount, irohaConsumer)
            .map { Unit }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
