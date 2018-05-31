package notary

import java.math.BigInteger

/**
 * All event [Notary] is waiting for.
 */
sealed class NotaryInputEvent {

    /**
     * Class represents events in Iroha chain
     */
    sealed class IrohaChainInputEvent : NotaryInputEvent() {

        /**
         * Event which raised on adding new peer in Iroha network
         */
        data class OnIrohaAddPeer(val address: String, val key: List<Byte>) : IrohaChainInputEvent()

        /**
         * Event which is raised when custodian transfer assets to notary account to withdraw asset
         *
         * @param asset is asset id in Iroha
         * @param amount of ethereum to withdraw
         */
        abstract class OnIrohaSideChainTransfer(
                val asset: String,
                val amount: BigInteger
        ) : IrohaChainInputEvent()
    }


    /**
     * Common class for all interested Ethereum events
     */
    sealed class EthChainInputEvent : NotaryInputEvent() {

        /**
         * Event which raised on deploying target smart contract in the Ethereum
         */
        abstract class OnEthDeployContract() : EthChainInputEvent()

        /**
         * Event which raised on new transfer transaction to Ethereum wallet
         * @param hash transaction hash
         * @param from transaction sender address
         * @param value amount of Ethereum transfered
         * @param input hex formatted transaction data
         */
        data class OnEthSidechainDeposit(
                val hash: String,
                val from: String,
                val value: BigInteger,
                val input: String
        ) : EthChainInputEvent()

        /**
         * Event which raised on new transaction with new peer in the contract
         */
        abstract class OnEthAddPeer : EthChainInputEvent()
    }

}
