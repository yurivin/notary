package registration

import com.github.kittinunf.result.Result
import jp.co.soramitsu.iroha.java.Utils
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sidechain.iroha.consumer.IrohaConsumer
import sidechain.iroha.util.ModelUtil

/**
 * Strategy to register client account in D3. This strategy creates only Iroha account.
 */
@Component
class NotaryRegistrationStrategy(
    @Autowired private val irohaConsumer: IrohaConsumer
) : RegistrationStrategy {

    /**
     * Register a new D3 client in Iroha
     * @param name - unique user name
     * @param domain - client domain
     * @param pubkey - client public key
     * @return hash of tx in Iroha
     */
    override fun register(
        name: String,
        domain: String,
        whitelist: List<String>,
        pubkey: String
    ): Result<String, Exception> {
        logger.info { "notary registration of client $name with pubkey $pubkey" }
        return ModelUtil.createAccount(
            irohaConsumer,
            name,
            domain,
            Utils.parseHexPublicKey(pubkey)
        )
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
