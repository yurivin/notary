package jp.co.soramitsu.notary.bootstrap

import com.fasterxml.jackson.databind.ObjectMapper
import jp.co.soramitsu.notary.bootstrap.dto.*
import jp.co.soramitsu.notary.bootstrap.genesis.d3.D3TestGenesisFactory
import mu.KLogging
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import sidechain.iroha.util.ModelUtil
import javax.xml.bind.DatatypeConverter
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IrohaTest {

    private val log = KLogging().logger

    @Autowired
    lateinit var mvc: MockMvc

    val d3Genesis = D3TestGenesisFactory()

    private val mapper = ObjectMapper()

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

    @Test
    fun testAccountsNeeded() {
        val result: MvcResult = mvc
            .perform(get("/iroha/config/accounts/D3/test"))
            .andExpect(status().isOk)
            .andReturn()

        val respBody = result.response.contentAsString
        val activeAccounts = ArrayList<AccountPrototype>()
        d3Genesis.getAccountsNeeded().forEach { if (!it.passive) activeAccounts.add(it) }
        assertEquals(mapper.writeValueAsString(activeAccounts), respBody)
    }

    @Test
    fun testGenesisBlock() {
        val peerKey1 = DatatypeConverter.printHexBinary(ModelUtil.generateKeypair().public.encoded)
        val peerKey2 = DatatypeConverter.printHexBinary(ModelUtil.generateKeypair().public.encoded)

        val result: MvcResult = mvc
            .perform(
                post("/iroha/create/genesisBlock").contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        GenesisRequest(
                            peers = listOf(
                                Peer(peerKey1, "firstTHost:12435"),
                                Peer(peerKey2, "secondTHost:987654")
                            ),
                            accounts = listOf()
                        )
                    )
                )
            )
            .andExpect(status().isOk)
            .andReturn()

        val respBody = result.response.contentAsString
        log.info("Response: $respBody")
    }

    /*val accounts: List<IrohaAccountDto>,
    val peers: List<Peer>,
    val blockVersion: String = "1",
    val meta: Project = Project()*/

}

