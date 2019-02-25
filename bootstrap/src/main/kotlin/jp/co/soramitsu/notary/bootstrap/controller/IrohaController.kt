package jp.co.soramitsu.notary.bootstrap.controller

import jp.co.soramitsu.notary.bootstrap.dto.GenesisData
import jp.co.soramitsu.notary.bootstrap.dto.KeyPairDto
import jp.co.soramitsu.notary.bootstrap.service.GenesisBlockService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sidechain.iroha.util.ModelUtil
import javax.xml.bind.DatatypeConverter

@RestController
@RequestMapping("/iroha")
class IrohaController(val blockService: GenesisBlockService) {

    private val log = KLogging().logger

    @GetMapping("/getKeyPair")
    fun generateBankAccount(): ResponseEntity<KeyPairDto> {
        log.info("Request to generate KeyPair")

        val keyPair = ModelUtil.generateKeypair()
        val response = KeyPairDto(DatatypeConverter.printHexBinary(keyPair.private.encoded), DatatypeConverter.printHexBinary(keyPair.public.encoded))
        return ResponseEntity.ok<KeyPairDto>(response)
    }

    @GetMapping("/getGenericBlock")
    fun generateGenericBlock():ResponseEntity<GenesisData> {
        log.info("Request of generic data")
        return ResponseEntity.ok<GenesisData>(blockService.getGenericData())
    }
}

