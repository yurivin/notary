package sidechain.iroha.util

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import iroha.protocol.QryResponses
import iroha.protocol.TransactionOuterClass
import jp.co.soramitsu.iroha.ModelQueryBuilder
import model.IrohaCredential
import sidechain.iroha.consumer.IrohaNetwork
import java.math.BigInteger

/**
 * Get asset precision
 *
 * @param queryCreator - iroha credentials for query creator
 * @param irohaNetwork - iroha network layer
 * @param assetId asset id in Iroha
 */
fun getAssetPrecision(
    queryCreator: IrohaCredential,
    irohaNetwork: IrohaNetwork,
    assetId: String
): Result<Short, Exception> {
    val uquery = ModelQueryBuilder().creatorAccountId(queryCreator.accountId)
        .queryCounter(BigInteger.valueOf(1))
        .createdTime(ModelUtil.getCurrentTime())
        .getAssetInfo(assetId)
        .build()

    return ModelUtil.prepareQuery(uquery, queryCreator.keyPair)
        .flatMap { query -> irohaNetwork.sendQuery(query) }
        .map { queryResponse ->
            validateResponse(queryResponse, "asset_response")
            queryResponse.assetResponse.asset.precision.toShort()
        }
}

/**
 * Retrieves account details from Iroha
 * @param queryCreator - iroha credentials for query creator
 * @param irohaNetwork - iroha network layer
 * @param acc account to retrieve relays from
 * @param detailSetterAccount - account that has set the details
 * @return Map with account details
 */
fun getAccountDetails(
    queryCreator: IrohaCredential,
    irohaNetwork: IrohaNetwork,
    acc: String,
    detailSetterAccount: String
): Result<Map<String, String>, Exception> {
    val uquery = ModelQueryBuilder().creatorAccountId(queryCreator.accountId)
        .queryCounter(BigInteger.valueOf(1))
        .createdTime(ModelUtil.getCurrentTime())
        .getAccount(acc)
        .build()

    return ModelUtil.prepareQuery(uquery, queryCreator.keyPair)
        .flatMap { query -> irohaNetwork.sendQuery(query) }
        .map { queryResponse ->
            validateResponse(queryResponse, "account_response")
            val account = queryResponse.accountResponse.account
            val stringBuilder = StringBuilder(account.jsonData)
            val json: JsonObject = Parser().parse(stringBuilder) as JsonObject

            if (json.map[detailSetterAccount] == null)
                mapOf()
            else
                json.map[detailSetterAccount] as Map<String, String>
        }
}

/**
 * Return first transaction from transaction query response
 * @param queryResponse - query response on getTransactions
 * @return first transaction
 */
fun getFirstTransaction(queryResponse: QryResponses.QueryResponse): Result<TransactionOuterClass.Transaction, Exception> {
    return Result.of {
        validateResponse(queryResponse, "transactions_response")
        if (queryResponse.transactionsResponse.transactionsCount == 0)
            throw Exception("There is no transactions.")

        // return transaction
        queryResponse.transactionsResponse.transactionsList[0]
    }
}

/**
 * Throws exception, if fieldName is not present
 * @param queryResponse - query response on getTransactions
 * @param fieldName - field name to validate
 */
private fun validateResponse(queryResponse: QryResponses.QueryResponse, fieldName: String) {
    val fieldDescriptor = queryResponse.descriptorForType.findFieldByName(fieldName)
    if (!queryResponse.hasField(fieldDescriptor)) {
        throw IllegalStateException("Query response error: ${queryResponse.errorResponse}")
    }
}
