package notary

import io.reactivex.Observable
import model.IrohaCredential
import notary.btc.BtcNotaryConfig
import notary.eth.EthNotaryConfig
import sidechain.SideChainEvent

fun createEthNotary(
    ethNotaryConfig: EthNotaryConfig,
    notaryCredential: IrohaCredential,
    queryCreator: IrohaCredential,
    ethEvents: Observable<SideChainEvent.PrimaryBlockChainEvent>
): NotaryImpl {
    return NotaryImpl(
        ethNotaryConfig.iroha,
        ethEvents,
        "ethereum",
        queryCreator, notaryCredential,
        ethNotaryConfig.notaryListStorageAccount,
        ethNotaryConfig.notaryListSetterAccount
    )
}

fun createBtcNotary(
    btcNotaryConfig: BtcNotaryConfig,
    notaryCredential: IrohaCredential,
    queryCreator: IrohaCredential,
    btcEvents: Observable<SideChainEvent.PrimaryBlockChainEvent>
): NotaryImpl {
    return NotaryImpl(
        btcNotaryConfig.iroha,
        btcEvents,
        "bitcoin",
        queryCreator,notaryCredential,
        btcNotaryConfig.notaryListStorageAccount,
        btcNotaryConfig.notaryListSetterAccount
    )
}
