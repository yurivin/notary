package integration.btc

import com.google.common.collect.ImmutableList
import integration.helper.IntegrationHelperUtil
import org.bitcoinj.core.*
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.store.LevelDBBlockStore
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.wallet.Wallet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BtcMultiSigTest {

    private val integrationHelper = IntegrationHelperUtil()

    init {
        //integrationHelper.generateBtcBlocks()
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

    @Test
    fun testSimpleMultiSig() {
        val file = File("deploy/bitcoin/regtest/d3.wallet")
        val addressStorage = Wallet.loadFromFile(file)
        val params = RegTestParams.get()
        val wal = Wallet(params)
        addressStorage.watchedAddresses.forEach { address -> wal.addWatchedAddress(address) }
        val blockStore = MemoryBlockStore(params)
        val chain = BlockChain(params, wal, blockStore)
        val peerGroup = PeerGroup(params, chain)

        peerGroup.startAsync()
        // Now download and process the block chain.
        peerGroup.downloadBlockChain()


        //peerGroup.stopAsync()

        val key1_1 = wal.freshReceiveKey()
        val key1_2 = wal.freshReceiveKey()

        val threshold = 2
        val rkeys1 = ImmutableList.of(key1_1, key1_2)
        val to_script1 = createMsAddress(rkeys1, threshold)

        val to_address1 = to_script1.getToAddress(params)

        addressStorage.addWatchedAddress(to_address1)
        wal.addWatchedAddress(to_address1)
        val freshAddress=wal.freshReceiveAddress()
        addressStorage.addWatchedAddress(freshAddress)
        val outaddress = Address.fromBase58(params, wal.freshReceiveAddress().toBase58())
        wal.addCoinsReceivedEventListener { wallet, tx, prevBalance, newBalance -> println("Received coins ${tx}") }
        wal.addCoinsSentEventListener { wallet, tx, prevBalance, newBalance -> print("Coins send ${tx}") }
        integrationHelper.sendBtc(to_address1.toString(), 5, 1)

        Thread.sleep(5_000)
        println(to_address1.toString())

        for (attempt in 1..10) {
            println("attempt $attempt")
            val unspents = ArrayList<TransactionOutput>()
            wal.unspents.forEach { tx ->
                val address = tx.getAddressFromP2SH(params)
                if (address == to_address1) {
                    println("Used unspent $tx")
                    unspents.add(tx)
                }
            }
            val redeem1 = ScriptBuilder.createMultiSigOutputScript(threshold, rkeys1 as List<ECKey>?)

            val out = unspents.elementAt(0)

            val spendTx = Transaction(params)
            spendTx.addOutput(Coin.valueOf(10_000), outaddress)
            spendTx.addOutput(out.value.subtract(Coin.valueOf(20_000)), to_address1)
            val input1 = spendTx.addInput(out)
            val hash_out1 = spendTx.hashForSignature(0, redeem1, Transaction.SigHash.ALL, false)
            val signature1_1 = TransactionSignature(key1_1.sign(hash_out1), Transaction.SigHash.ALL, false)
            val signature1_2 = TransactionSignature(key1_2.sign(hash_out1), Transaction.SigHash.ALL, false)

            val inputScript1 = ScriptBuilder.createP2SHMultiSigInputScript(
                listOf(
                    signature1_1,
                    signature1_2
                ),
                redeem1
            )
            input1.scriptSig = inputScript1
            input1.verify(input1.connectedOutput)
            // Send to bitcoin node
            peerGroup.broadcastTransaction(spendTx)
            integrationHelper.generateBtcBlocks(1)
            Thread.sleep(5000)
            addressStorage.saveToFile(file)
        }
    }
}
