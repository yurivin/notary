package jp.co.soramitsu.notary.bootstrap.dto

import java.security.KeyPair
import javax.xml.bind.DatatypeConverter

private interface DtoFactory<out T> {
    fun getDTO(): T
}

open class Conflicatable(var errorCode:Int? = null, var message:String? = null)

data class BlockchainCreds(val private:String? = null, val public:String? = null, val address:String? = null)
data class IrohaAccountDto(val title:String, val domainId:String, val creds: HashSet<BlockchainCreds>)

data class IrohaAccount(val title:String, val domain: String, val keys: HashSet<KeyPair>) : DtoFactory<IrohaAccountDto> {
    override fun getDTO(): IrohaAccountDto {
       val credsList:HashSet<BlockchainCreds> = HashSet()
        keys.forEach {
            credsList.add(BlockchainCreds(DatatypeConverter.printHexBinary(it.private.encoded),DatatypeConverter.printHexBinary(it.public.encoded)))
        }
       return IrohaAccountDto(this.title, domain, credsList)
    }
}

data class Peer(val peerKey:String, val hostPort:String)
data class Project(val project:String = "D3", val environment:String = "test")

data class GenesisRequest(val accounts:List<IrohaAccountDto>, val peers:List<Peer>, val blockVersion:String = "1", val meta:Project = Project())
data class GenesisData(val blockData:String? = null) : Conflicatable()