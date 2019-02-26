package jp.co.soramitsu.notary.bootstrap.genesis

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import java.security.PublicKey
import javax.xml.bind.DatatypeConverter

fun getIrohaPublicKeyFromHexString(hex:String): PublicKey {
    return Ed25519Sha3.publicKeyFromBytes(DatatypeConverter.parseHexBinary(hex))
}

