package sideChain.iroha

import notary.NotaryInputEvent
import com.nhaarman.mockito_kotlin.mock
import mu.KLogging
import notary.IrohaCommand
import sideChain.ChainHandler

/**
 * Dummy implementation of [ChainHandler] with effective dependencies
 */
class IrohaChainHandlerStub : ChainHandler<IrohaBlockStub> {

    /**
     * TODO Replace dummy with effective implementation
     */
    override fun parseBlock(block: IrohaBlockStub): List<NotaryInputEvent> {
        logger.info { "Iroha chain handler" }
        return block.transactions
                .flatMap { it.commands }
                .filter { it is IrohaCommand.CommandAddPeer }
                .map {
                    it as IrohaCommand.CommandAddPeer
                    NotaryInputEvent.IrohaChainInputEvent.OnIrohaAddPeer(it.address, it.peerKey)
                }
    }

    /**
     * Logger
     */
    companion object : KLogging()
}
