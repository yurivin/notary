package jp.co.soramitsu.notary.bootstrap.service

import com.google.protobuf.util.JsonFormat
import iroha.protocol.BlockOuterClass
import iroha.protocol.Primitive
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import jp.co.soramitsu.notary.bootstrap.dto.Account
import jp.co.soramitsu.notary.bootstrap.dto.Domain
import jp.co.soramitsu.notary.bootstrap.dto.GenesisData
import java.math.BigDecimal

class GenesisBlockServiceImpl : GenesisBlockService {

    private val userRole = "user"
    private val usdName = "usd"

    private val crypto = Ed25519Sha3()

    private val peerKeypair = crypto.generateKeypair()

    private fun user(name: String): String {
        return String.format("%s@%s", name, Domain.bank.name)
    }

    private val usd = String.format("%s#%s", usdName, Domain.bank.name)

    override fun getGenericData(): GenesisData {
        val accountA = Account("AccountA", crypto.generateKeypair(), Domain.bank)
        val accountB = Account("AccountB", crypto.generateKeypair(), Domain.bank)


        val block: BlockOuterClass.Block = GenesisBlockBuilder()
            // first transaction
            .addTransaction(
                // transactions in genesis block can have no creator
                Transaction.builder(null)
                    // by default peer is listening on port 10001
                    .addPeer("0.0.0.0:10001", peerKeypair.getPublic())
                    // create default "user" role
                    .createRole(
                        userRole,
                        listOf(
                            Primitive.RolePermission.can_transfer,
                            Primitive.RolePermission.can_get_my_acc_ast,
                            Primitive.RolePermission.can_get_my_txs,
                            Primitive.RolePermission.can_receive
                        )
                    )
                    .createDomain(Domain.bank.name, userRole)
                    // create user A
                    .createAccount(accountA.title, Domain.bank.name, accountA.keys.public)
                    // create user B
                    .createAccount(accountB.title, Domain.bank.name, accountB.keys.public)
                    // create usd#bank with precision 2
                    .createAsset(usdName, Domain.bank.name, 2)
                    // transactions in genesis block can be unsigned
                    .build() // returns ipj model Transaction
                    .build() // returns unsigned protobuf Transaction
            )
            // we want to increase user_a balance by 100 usd
            .addTransaction(
                Transaction.builder(user(accountA.title))
                    .addAssetQuantity(usd, BigDecimal("100"))
                    .build()
                    .build()
            ).build()
        val json = JsonFormat.printer().print(block)
        return GenesisData(json, listOf(accountA, accountB))
    }

}