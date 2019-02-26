package jp.co.soramitsu.notary.bootstrap.genesis

import jp.co.soramitsu.notary.bootstrap.dto.IrohaAccountDto
import jp.co.soramitsu.notary.bootstrap.dto.Peer

class D3TestGenesisFactory : GenesisInterface {
    override fun getProject():String {
        return "D3"
    }

    override fun getEnvironment():String {
        return "test"
    }

    override fun createGenesisBlock(accounts: List<IrohaAccountDto>, peers: List<Peer>, blockVersion: String):String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}