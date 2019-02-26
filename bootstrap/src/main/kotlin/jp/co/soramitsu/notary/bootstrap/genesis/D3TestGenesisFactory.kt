package jp.co.soramitsu.notary.bootstrap.genesis

import com.google.protobuf.util.JsonFormat
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import jp.co.soramitsu.notary.bootstrap.dto.IrohaAccountDto
import jp.co.soramitsu.notary.bootstrap.dto.Peer

class D3TestGenesisFactory : GenesisInterface {

    override fun getProject(): String {
        return "D3"
    }

    override fun getEnvironment(): String {
        return "test"
    }

    override fun createGenesisBlock(accounts: List<IrohaAccountDto>, peers: List<Peer>, blockVersion: String): String {
        val builder = GenesisBlockBuilder()
        
        createPeers(peers, builder)
        createRoles(builder)

        val block = builder.build()
        return JsonFormat.printer().print(block)
    }

    private fun createPeers(
        peers: List<Peer>,
        builder: GenesisBlockBuilder
    ) {
        peers.forEach {
            builder.addTransaction(
                Transaction.builder(null)
                    .addPeer(it.hostPort, getIrohaPublicKeyFromHexString(it.peerKey))
                    .build() // returns ipj model Transaction
                    .build()
            )
        }
    }

    private fun createRoles(builder: GenesisBlockBuilder) {
        createNotaryRole(builder)
        createRelayDeployerRole(builder)
        createEthTokenListStorageRole(builder)
        createRegistrationServiceRole(builder)
        createClientRole(builder)
        createWithdrawalRole(builder)
        createSignatureCollectorRole(builder)
        createVacuumerRole(builder)
        createNoneRole(builder)
        createTesterRole(builder)
        createWhiteListSetterRole(builder)
        createRollBackRole(builder)
        createNotaryListHolderRole(builder)
        createSoraClientRole(builder)
        createBtcFeeRateSetterRole(builder)
    }

}
