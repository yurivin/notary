package jp.co.soramitsu.notary.bootstrap

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.util.JsonFormat
import io.ktor.http.cio.expectHttpBody
import iroha.protocol.BlockOuterClass
import iroha.protocol.Primitive
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import mu.KLogging
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IrohaTest {

    private val log = KLogging().logger

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun keyPairTest() {
        val result: MvcResult = mvc
            .perform(get("/iroha/create/keyPair"))
            .andExpect(status().isOk)
            .andReturn()
        val respBody = result.response.contentAsString
        log.info("Response body: $respBody")
        assertTrue(respBody.contains("private"))
        assertTrue(respBody.contains("public"))
    }

    @Test
    fun testProjectsGenesis() {
        val result: MvcResult = mvc
            .perform(get("/iroha/projects/genesis"))
            .andExpect(status().isOk)
            .andReturn()
        val respBody = result.response.contentAsString
        assertEquals("{\"D3\":\"test\"}", respBody)
    }

}

