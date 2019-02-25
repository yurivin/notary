package jp.co.soramitsu.notary.bootstrap.dto

data class KeyPairDTO(val private:String, val public:String)

data class Account(val title:String, val keys:KeyPairDTO)

data class GenericData(val blcokData:String, val accounts:List<Account>)