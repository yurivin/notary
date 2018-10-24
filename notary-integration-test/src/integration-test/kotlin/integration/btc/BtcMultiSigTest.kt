package integration.btc

import com.google.common.collect.ImmutableList
import integration.helper.IntegrationHelperUtil
import org.bitcoinj.core.*
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.wallet.Wallet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BtcMultiSigTest {

    private val integrationHelper = IntegrationHelperUtil()

    private val btcPreGenConfig =
        integrationHelper.configHelper.createBtcPreGenConfig()

    init {
        integrationHelper.generateBtcBlocks()
        //integrationHelper.addNotary("test_notary", "test_notary_address")
        //btcNotaryInitialization.init().failure { ex -> fail("Cannot run BTC notary", ex) }
    }

    private fun createMsAddress(notaryKeys: Collection<DeterministicKey>, threshold: Int): Script {
        val keys = ArrayList<ECKey>()
        notaryKeys.forEach { key ->
            val ecKey = ECKey.fromPublicOnly(key.pubKey)
            keys.add(ecKey)
        }
        return ScriptBuilder.createP2SHOutputScript(threshold, keys)
    }

    /**
     * Test US-001 Deposit
     * Note: Iroha and bitcoind must be deployed to pass the test.
     * @given new registered account]\
     * @when 1 btc was sent to new account
     * @then balance of new account is increased by 1 btc(or 100.000.000 sat)
     */
    @Test
    fun testSimpleMultiSig() {
        val file = File(btcPreGenConfig.btcWalletFilePath)
        val wal = Wallet.loadFromFile(file)
        // One multisig address
        println(btcPreGenConfig.btcWalletFilePath)

        val parms = RegTestParams.get()

        val blockStore = MemoryBlockStore(parms)
        val chain = BlockChain(parms, wal, blockStore)

        val peerGroup = PeerGroup(parms, chain)

        wal.addCoinsReceivedEventListener { wallet, tx, prevBalance, newBalance ->
            println("Received tokens ")
        }

        peerGroup.startAsync()

        // Now download and process the block chain.
        peerGroup.downloadBlockChain()
        //peerGroup.stopAsync()


        val key1_1 = wal.freshReceiveKey()
        val key1_2 = wal.freshReceiveKey()
        println(key1_1)
        val key2_1 = wal.freshReceiveKey()
        val key2_2 = wal.freshReceiveKey()

        val threshold = 2
        val rkeys1 = ImmutableList.of(key1_1, key1_2)
        val rkeys2 = ImmutableList.of(key2_1, key2_2)
        val to_script1 = createMsAddress(rkeys1, threshold)
        val to_script2 = createMsAddress(rkeys2, threshold)

        val to_address1 = to_script1.getToAddress(RegTestParams.get())
        val to_address2 = to_script2.getToAddress(RegTestParams.get())


        wal.addWatchedAddress(to_address1)
        wal.addWatchedAddress(to_address2)


        integrationHelper.sendBtc(to_address1.toString(), 1)
        integrationHelper.sendBtc(to_address2.toString(), 1)
        Thread.sleep(10_000)
        println("Walet balance ${wal.balance}")
        println(to_address1.toString())

        val unspents = ArrayList<TransactionOutput>()
        wal.unspents.forEach { tx ->
            println("Unspend transaction output $tx")
            val address = tx.getAddressFromP2SH(RegTestParams.get())
            if (address != null) {
                unspents.add(tx)
            }
        }

        val redeem1 = ScriptBuilder.createRedeemScript(threshold, rkeys1 as List<ECKey>?)
        val redeem2 = ScriptBuilder.createRedeemScript(threshold, rkeys2 as List<ECKey>?)


        val out = unspents.elementAt(0)
        val out2 = unspents.elementAt(1)
        val fee = Coin.valueOf( 1000) // fee in satoshi
        val value = out.value + out2.value - fee


        val spendTx = Transaction(RegTestParams.get())
        // Add output where to send mnkCskVHPmLop5qHoHa1WH8EoUCJuxPGQs]
        val outaddress = Address.fromBase58(RegTestParams.get(), "mnkCskVHPmLop5qHoHa1WH8EoUCJuxPGQs")
        spendTx.addOutput(Coin.valueOf(value.value), outaddress)
        // Add input from which take the
        val input1 = spendTx.addInput(out)
        val input2 = spendTx.addInput(out2)

        // Hash for first input
        val hash_out1 = spendTx.hashForSignature(0, redeem1, Transaction.SigHash.ALL, false)
        val hash_out2 = spendTx.hashForSignature(1, redeem2, Transaction.SigHash.ALL, false)
        // Signatures from first notary
        val signature1_1 = TransactionSignature(key1_1.sign(hash_out1), Transaction.SigHash.ALL, false)
        val signature2_1 = TransactionSignature(key2_1.sign(hash_out2), Transaction.SigHash.ALL, false)
        // Send it somethere
        // .....
        // Siganture from second notary
        val signature1_2 = TransactionSignature(key1_2.sign(hash_out1), Transaction.SigHash.ALL, false)
        val signature2_2 = TransactionSignature(key2_2.sign(hash_out2), Transaction.SigHash.ALL, false)


        // When collected all needed (>= threshold signatures) - spend tokens
        // Create the script that spends the multi-sig output.
        val inputScript1 = ScriptBuilder.createP2SHMultiSigInputScript(
            listOf(
                signature1_1,
                signature1_2
            ),
            redeem1
        )

        val inputScript2 = ScriptBuilder.createP2SHMultiSigInputScript(
            listOf(
                signature2_1,
                signature2_2
            ),
            redeem2
        )

        // Add signatures to the fisrt input.
        input1.scriptSig = inputScript1
        input1.verify()


        // Add signatures to the second input.
        input2.scriptSig = inputScript2
        input2.verify()


        println("Sending transaction $spendTx")

        // Send to bitcoin node
        peerGroup.broadcastTransaction(spendTx)

        assertEquals(1, wal.balance.value)
        wal.saveToFile(file)
        peerGroup.stopAsync()

    }
}
