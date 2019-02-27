package jp.co.soramitsu.notary.bootstrap.genesis

import com.google.protobuf.util.JsonFormat
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.java.TransactionBuilder
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
        val transactionBuilder = Transaction.builder(null)

        createPeers(peers, transactionBuilder)
        createRoles(transactionBuilder)

        val blockBuilder = GenesisBlockBuilder().addTransaction(transactionBuilder.build().build())
        val block = blockBuilder.build()
        return JsonFormat.printer().print(block)
    }

    /*fun createDomain()*/

    private fun createPeers(
        peers: List<Peer>,
        builder: TransactionBuilder
    ) {
        peers.forEach {
            builder
                .addPeer(it.hostPort, getIrohaPublicKeyFromHexString(it.peerKey))
        }
    }

    /*  {
          "createDomain": {
          "domainId": "notary",
          "defaultRole": "none"
      }
      },
      {
          "createDomain": {
          "domainId": "d3",
          "defaultRole": "client"
      }
      },
      {
          "createDomain": {
          "domainId": "btcSession",
          "defaultRole": "none"
      }
      },
      {
          "createDomain": {
          "domainId": "ethereum",
          "defaultRole": "none"
      }
      },
      {
          "createDomain": {
          "domainId": "sora",
          "defaultRole": "sora_client"
      }
      },
      {
          "createAsset": {
          "assetName": "xor",
          "domainId": "sora",
          "precision": 0
      }
      },
      {
          "createAsset": {
          "assetName": "ether",
          "domainId": "ethereum",
          "precision": 18
      }
      },
      {
          "createDomain": {
          "domainId": "bitcoin",
          "defaultRole": "none"
      }
      },
      {
          "createDomain": {
          "domainId": "btcSignCollect",
          "defaultRole": "none"
      }
      },
      {
          "createAsset": {
          "assetName": "btc",
          "domainId": "bitcoin",
          "precision": 8
      }
      },*/

    private fun createRoles(builder: TransactionBuilder) {
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
