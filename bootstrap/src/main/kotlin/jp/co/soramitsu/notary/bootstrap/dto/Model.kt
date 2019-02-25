package jp.co.soramitsu.notary.bootstrap.dto

import jp.co.soramitsu.notary.bootstrap.service.GenesisBlockServiceImpl
import java.security.KeyPair
import javax.xml.bind.DatatypeConverter

private interface DtoFactory<out T> {
    fun getDTO(): T
}

data class KeyPairDto(val private:String, val public:String)
data class AccountDto(val title:String, val keys: KeyPairDto)

data class Account(val title:String, val keys: KeyPair, val domain: Domain) : DtoFactory<AccountDto> {
    override fun getDTO(): AccountDto {
       return AccountDto(this.title, KeyPairDto(DatatypeConverter.printHexBinary(keys.private.encoded), DatatypeConverter.printHexBinary(keys.public.encoded)))
    }
}

data class GenesisData(val blockData:String, val accounts:List<Account>)