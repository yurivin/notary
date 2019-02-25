package jp.co.soramitsu.notary.bootstrap.controller

import jp.co.soramitsu.notary.bootstrap.dto.KeyPairDTO
import mu.KLogging
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
    fun createBankAccount(): ResponseEntity<KeyPairDTO> {
        log.info("REST request to generate KeyPair")

        val keyPair = ModelUtil.generateKeypair()
        val response = KeyPairDTO(keyPair.private.toString(), keyPair.public.toString())
        return ResponseEntity.created(URI("/iroha/generateKeyPair"))
                .body<KeyPairDTO>(response)
    }
}

