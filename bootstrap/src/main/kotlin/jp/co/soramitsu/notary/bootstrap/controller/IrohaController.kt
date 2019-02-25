package jp.co.soramitsu.notary.bootstrap.controller

import jp.co.soramitsu.iroha.java.Utils
import jp.co.soramitsu.notary.bootstrap.dto.GenericData
import jp.co.soramitsu.notary.bootstrap.dto.KeyPairDTO
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sidechain.iroha.util.ModelUtil
import java.net.URI


@RestController
@RequestMapping("/iroha")
class IrohaController {

    private val log = KLogging().logger

    @GetMapping("/generateKeyPair")
    fun generateBankAccount(): ResponseEntity<KeyPairDTO> {
        log.info("REST request to generate KeyPair")

        val keyPair = ModelUtil.generateKeypair()
        val response = KeyPairDTO(Utils.toHex(keyPair.private.encoded), Utils.toHex(keyPair.public.encoded))
        return ResponseEntity.ok<KeyPairDTO>(response)
    }

   /* @GetMapping("/generateGenericBlock")
    fun generateGenericBlock():ResponseEntity<GenericData> {
        log.info("Request of generic data")

    }*/
}

