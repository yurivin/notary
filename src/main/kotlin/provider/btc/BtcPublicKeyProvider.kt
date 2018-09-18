package provider.btc

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import mu.KLogging
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.Wallet
import provider.NotaryPeerListProvider
import sidechain.iroha.consumer.IrohaConsumer
import sidechain.iroha.util.ModelUtil
import util.getRandomId
import java.io.File

/**
 *  Bitcoin keys provider
 *  @param wallet - bitcoinJ wallet class
 *  @param walletFile - file where to safe wallet
 *  @param irohaConsumer
 *  @param notaryPeerListProvider - Provider that helps us to fetch all the peers registered in the network
 *  @param btcRegistrationAccount - registration account for new account with btc sidechain
 *  @param mstBtcRegistrationAccount - BTC registration account, that works in MST fashion
 *  @param mappingAccount - Notary account to store BTC addresses
 */
class BtcPublicKeyProvider(
    private val wallet: Wallet,
    private val walletFile: File,
    private val irohaConsumer: IrohaConsumer,
    private val notaryPeerListProvider: NotaryPeerListProvider,
    private val btcRegistrationAccount: String,
    private val mstBtcRegistrationAccount: String,
    private val mappingAccount: String
) {

    private val sessionDomain = "btcSession"

    /**
     * Creates notary public key and sets it into session account details
     * @param sessionAccountName - name of session account
     * @return new public key created by notary
     */
    fun createKey(sessionAccountName: String): Result<String, Exception> {
        // Generate new key from wallet
        val key = wallet.freshReceiveKey()
        val pubKey = key.publicKeyAsHex
        return ModelUtil.setAccountDetail(
            irohaConsumer,
            btcRegistrationAccount,
            "$sessionAccountName@$sessionDomain",
            String.getRandomId(),
            pubKey
        ).map {
            wallet.saveToFile(walletFile)
            pubKey
        }
    }

    /**
     * Creates multisignature address if enough public keys are provided
     * @param notaryKeys - list of all notaries public keys
     * @return Result of operation
     */
    fun checkAndCreateMultiSigAddress(notaryKeys: Collection<String>): Result<Unit, Exception> {
        return Result.of {
            val peers = notaryPeerListProvider.getPeerList().size
            if (peers == 0) {
                throw IllegalStateException("No peers to create btc multisignature address")
            } else if (notaryKeys.size == peers && hasMyKey(notaryKeys)) {
                val threshold = getThreshold(peers)
                val msAddress = createMultiSigAddress(notaryKeys, threshold)
                wallet.addWatchedAddress(msAddress)
                ModelUtil.setAccountDetail(
                    irohaConsumer,
                    mstBtcRegistrationAccount,
                    mappingAccount,
                    msAddress.toBase58(),
                    "free"
                ).fold({
                    wallet.saveToFile(walletFile)
                    logger.info { "New BTC multisignature address $msAddress was created " }
                }, { ex -> throw ex })
            }
        }
    }

    /**
     * Checks if current notary has its key in notaryKeys
     * @param notaryKeys - public keys of notaries
     * @return true if at least one current notary key is among given notaryKeys
     */
    private fun hasMyKey(notaryKeys: Collection<String>): Boolean {
        val hasMyKey = notaryKeys.find { key ->
            wallet.issuedReceiveKeys.find { ecKey -> ecKey.publicKeyAsHex == key } != null
        } != null
        return hasMyKey
    }

    /**
     * Creates multi signature bitcoin address
     * @param notaryKeys - public keys of notaries
     * @return created address
     */
    private fun createMultiSigAddress(notaryKeys: Collection<String>, threshold: Int): Address {
        val keys = ArrayList<ECKey>()
        notaryKeys.forEach { key ->
            val ecKey = ECKey.fromPublicOnly(Utils.parseAsHexOrBase58(key))
            keys.add(ecKey)
        }
        val script = ScriptBuilder.createP2SHOutputScript(threshold, keys)
        // TODO: Bulat D3-379 change to configurable: Maintest, Testnet etc.
        return script.getToAddress(RegTestParams.get())
    }

    /**
     * Calculate threshold
     * @param peers - total number of peers
     * @return minimal number of signatures required
     */
    private fun getThreshold(peers: Int): Int {
        return (peers * 2 / 3) + 1;
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
