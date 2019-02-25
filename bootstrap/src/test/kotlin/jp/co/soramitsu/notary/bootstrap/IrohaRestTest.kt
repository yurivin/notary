package jp.co.soramitsu.notary.bootstrap

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertTrue


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IrohaRestTest {

    private val logger = KLogging().logger

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun exampleTest() {
        val result: MvcResult = mvc!!
            .perform(get("/iroha/generateKeyPair"))
            .andDo { resp -> logger.info(resp.response.contentAsString) }
            .andExpect(status().isOk)
            .andReturn()
        val respBody = result.response.contentAsString
        assertTrue(respBody.contains("private"))
        assertTrue(respBody.contains("public"))
    }
}

