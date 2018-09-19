package registration

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import mu.KLogging
import notary.IrohaCommand
import notary.IrohaTransaction
import sidechain.iroha.consumer.IrohaConsumer
import sidechain.iroha.consumer.IrohaConverterImpl
import sidechain.iroha.util.ModelUtil.getCurrentTime

/**
 * Class for creating new accounts in Iroha
 * @param irohaConsumer - iroha interaction class
 * @param sidechainAddressStorageAccount - account where in details list of account_id-sidechain address will be stored
 * @param creator - creator account for new accounts
 * @param addressName - key value to put the address
 */
class IrohaAccountCreator(
    private val irohaConsumer: IrohaConsumer,
    private val sidechainAddressStorageAccount: String,
    private val creator: String,
    private val addressName: String
) {
    /**
     * Creates new account to Iroha with given address
     * - CreateAccount with client name
     * - SetAccountDetail on client account with assigned relay wallet from notary pool of free relay addresses
     * - SetAccountDetail on relayStorageAccount to mark relay address in pool as assigned to the particular user
     * @param currencyAddress - address of crypto currency wallet
     * @param userName - client userName in Irohaс
     * @param pubkey - client's public key
     * @param domain - client domain for all accounts
     * @return address associated with userName
     */
    fun create(
        currencyAddress: String,
        userName: String,
        domain: String,
        pubkey: String
    ): Result<String, Exception> {
        return Result.of {
            IrohaTransaction(
                creator,
                getCurrentTime(),
                1,
                arrayListOf(
                    // Create account
                    IrohaCommand.CommandCreateAccount(
                        userName, domain, pubkey
                    ),
                    // Set user wallet/address in account detail
                    IrohaCommand.CommandSetAccountDetail(
                        "$userName@$domain",
                        addressName,
                        currencyAddress
                    ),
                    // Set wallet/address as occupied by user id
                    IrohaCommand.CommandSetAccountDetail(
                        sidechainAddressStorageAccount,
                        currencyAddress,
                        "$userName@$domain"
                    )
                )
            )
        }.flatMap { irohaTx ->
            val utx = IrohaConverterImpl().convert(irohaTx)
            irohaConsumer.sendAndCheck(utx)
        }.map {
            logger.info { "New account $userName was created" }
            currencyAddress
        }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
