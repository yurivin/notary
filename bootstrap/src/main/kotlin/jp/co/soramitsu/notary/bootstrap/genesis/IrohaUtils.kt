package jp.co.soramitsu.notary.bootstrap.genesis

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.TransactionBuilder
import jp.co.soramitsu.notary.bootstrap.dto.Peer
import java.security.PublicKey
import javax.xml.bind.DatatypeConverter

fun getIrohaPublicKeyFromHexString(hex: String): PublicKey {
    return Ed25519Sha3.publicKeyFromBytes(DatatypeConverter.parseHexBinary(hex))
}


fun createDomain(
    builder: TransactionBuilder,
    domainId: String,
    defaultRole: String
) {
    builder.createDomain(domainId, defaultRole)
}

fun createAsset(
    builder: TransactionBuilder,
    name: String,
    domain: String,
    precision: Int
) {
    builder.createAsset(name, domain, precision)
}

fun createAccount(
    builder: TransactionBuilder,
    name: String,
    domainId: String,
    publicKey: String
) {
    builder.createAccount(name, domainId, getIrohaPublicKeyFromHexString(publicKey))

}

fun createPeers(
    peers: List<Peer>,
    builder: TransactionBuilder
) {
    peers.forEach {
        builder
            .addPeer(it.hostPort, getIrohaPublicKeyFromHexString(it.peerKey))
    }
}

