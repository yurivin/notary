package jp.co.soramitsu.notary.bootstrap.genesis.d3

import com.google.protobuf.util.JsonFormat
import com.sun.javaws.exceptions.InvalidArgumentException
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.java.TransactionBuilder
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import jp.co.soramitsu.notary.bootstrap.dto.IrohaAccountDto
import jp.co.soramitsu.notary.bootstrap.dto.Peer
import jp.co.soramitsu.notary.bootstrap.error.AccountException
import jp.co.soramitsu.notary.bootstrap.genesis.*

class D3TestGenesisFactory : GenesisInterface {

    override fun getProject(): String {
        return "D3"
    }

    override fun getEnvironment(): String {
        return "test"
    }

    override fun createGenesisBlock(
        accounts: List<IrohaAccountDto>,
        peers: List<Peer>,
        blockVersion: String
    ): String {
        val transactionBuilder = Transaction.builder(null)

        createPeers(peers, transactionBuilder)
        createRoles(transactionBuilder)
        createDomains(transactionBuilder)
        createAssets(transactionBuilder)
        createAccounts(transactionBuilder, accounts)

        val blockBuilder = GenesisBlockBuilder().addTransaction(transactionBuilder.build().build())
        val block = blockBuilder.build()
        return JsonFormat.printer().print(block)
    }

    private fun createAccounts(
        transactionBuilder: TransactionBuilder,
        accountsList: List<IrohaAccountDto>
    ) {
        val accountsMap: HashMap<String, IrohaAccountDto> = HashMap()
        accountsList.forEach { accountsMap.putIfAbsent("${it.title}@${it.domainId}", it) }

        val accountErrors = checkNeendedAccountsGiven(accountsMap)
        if (accountErrors.isNotEmpty()) {
            throw AccountException(accountErrors.toString())
        }
    }

    private fun checkNeendedAccountsGiven(accountsMap: HashMap<String, IrohaAccountDto>): List<String> {
        val loosed = ArrayList<String>()
        D3Context.d3neededAccounts.forEach {
            if (!accountsMap.containsKey(it.id)) {
                loosed.add("Needed account keys are not received: ${it.id}")
            }
        }
        return loosed
    }


    private fun createAssets(builder: TransactionBuilder) {
        createAsset(builder, "xor", "sora", 0)
        createAsset(builder, "ether", "ethereum", 18)
        createAsset(builder, "btc", "bitcoin", 8)
    }

    private fun createDomains(builder: TransactionBuilder) {
        createDomain(builder, "notary", "none")
        createDomain(builder, "d3", "client")
        createDomain(builder, "btcSession", "none")
        createDomain(builder, "ethereum", "none")
        createDomain(builder, "sora", "sora_client")
        createDomain(builder, "bitcoin", "client")
        createDomain(builder, "btcSignCollect", "none")
    }


    private fun createRoles(builder: TransactionBuilder) {
        D3Context.createNotaryRole(builder)
        D3Context.createRelayDeployerRole(builder)
        D3Context.createEthTokenListStorageRole(builder)
        D3Context.createRegistrationServiceRole(builder)
        D3Context.createClientRole(builder)
        D3Context.createWithdrawalRole(builder)
        D3Context.createSignatureCollectorRole(builder)
        D3Context.createVacuumerRole(builder)
        D3Context.createNoneRole(builder)
        D3Context.createTesterRole(builder)
        D3Context.createWhiteListSetterRole(builder)
        D3Context.createRollBackRole(builder)
        D3Context.createNotaryListHolderRole(builder)
        D3Context.createSoraClientRole(builder)
        D3Context.createBtcFeeRateSetterRole(builder)
    }

}
