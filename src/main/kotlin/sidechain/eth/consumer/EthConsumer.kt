package sidechain.eth.consumer

import config.EthereumConfig
import config.EthereumPasswords
import mu.KLogging
import org.web3j.utils.Numeric
import sidechain.eth.util.DeployHelper
import withdrawalservice.WithdrawalServiceOutputEvent
import java.math.BigDecimal
import java.math.BigInteger

class EthConsumer(ethereumConfig: EthereumConfig, ethereumPasswords: EthereumPasswords) {
    private val deployHelper = DeployHelper(ethereumConfig, ethereumPasswords)

    fun consume(event: WithdrawalServiceOutputEvent) {
        logger.info { "consumed eth event" }
        if (event is WithdrawalServiceOutputEvent.EthRefund) {
            logger.info {
                "Got proof:\n" +
                        "account ${event.proof.account}\n" +
                        "amount ${event.proof.amount}\n" +
                        "token ${event.proof.tokenContractAddress}\n" +
                        "iroha hash ${event.proof.irohaHash}\n" +
                        "relay ${event.proof.relay}\n"
            }

            val relay = contract.Relay.load(
                event.proof.relay,
                deployHelper.web3,
                deployHelper.credentials,
                deployHelper.gasPrice,
                deployHelper.gasLimit
            )

            val n = BigDecimal(event.proof.amount)
            val new_amount = n.scaleByPowerOfTen(18)

            relay.withdraw(
                event.proof.tokenContractAddress,
                BigInteger(new_amount.toPlainString()),
                event.proof.account,
                Numeric.hexStringToByteArray(event.proof.irohaHash),
                event.proof.v,
                event.proof.r,
                event.proof.s
            ).sendAsync()
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
