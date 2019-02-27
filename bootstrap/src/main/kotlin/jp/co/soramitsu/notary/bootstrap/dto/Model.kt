package jp.co.soramitsu.notary.bootstrap.dto

import jp.co.soramitsu.iroha.java.TransactionBuilder
import jp.co.soramitsu.notary.bootstrap.genesis.getIrohaPublicKeyFromHexString
import java.security.KeyPair
import javax.xml.bind.DatatypeConverter

private interface DtoFactory<out T> {
    fun getDTO(): T
}

open class Conflicatable(var errorCode: String? = null, var message: String? = null)

data class BlockchainCreds(
    val private: String? = null,
    val public: String? = null,
    val address: String? = null
)

data class IrohaAccountDto(
    val title: String,
    val domainId: String,
    val creds: HashSet<BlockchainCreds>
)

data class IrohaAccount(val title: String, val domain: String, val keys: HashSet<KeyPair>) :
    DtoFactory<IrohaAccountDto> {
    override fun getDTO(): IrohaAccountDto {
        val credsList: HashSet<BlockchainCreds> = HashSet()
        keys.forEach {
            credsList.add(
                BlockchainCreds(
                    DatatypeConverter.printHexBinary(it.private.encoded),
                    DatatypeConverter.printHexBinary(it.public.encoded)
                )
            )
        }
        return IrohaAccountDto(this.title, domain, credsList)
    }
}

data class Peer(val peerKey: String, val hostPort: String)
data class Project(val project: String = "D3", val environment: String = "test")

data class GenesisRequest(
    val accounts: List<IrohaAccountDto>,
    val peers: List<Peer>,
    val blockVersion: String = "1",
    val meta: Project = Project()
)

data class GenesisData(val blockData: String? = null) : Conflicatable()

class PassiveAccountPrototype(
    name: String,
    domainId: String,
    roles: List<String> = listOf(),
    details: HashMap<String, String> = HashMap(),
    quorum:Int = 1
) : AccountPrototype(name,domainId,roles,details,passive = true,quorum = quorum) {

    fun createAccount(builder: TransactionBuilder) {
        createAccount(builder)
    }

    override fun createAccount(
        builder: TransactionBuilder,
        publicKey: String
    ) {
        createAccount(builder)
    }
}


open class AccountPrototype(
    val name: String,
    val domainId: String,
    private val roles: List<String> = listOf(),
    private val details: Map<String, String> = mapOf(),
    val passive:Boolean = false,
    val quorum:Int = 1
) {
    val id = "$name@$domainId"

    open fun createAccount(builder: TransactionBuilder, publicKey: String = "0000000000000000000000000000000000000000000000000000000000000000") {
        builder.createAccount(name, domainId, getIrohaPublicKeyFromHexString(publicKey))
        roles.forEach { builder.appendRole(id, it) }
        details.forEach { k, v -> builder.setAccountDetail(id,k,v) }
    }
}