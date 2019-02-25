@file:JvmName("RegistrationMain")

package jp.co.soramitsu.notary.bootstrap

import config.getProfile
import mu.KLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["jp.co.soramitsu.notary.bootstrap"])
class BootstrapMain

    private val logger = KLogging().logger

    fun main(args: Array<String>) {
        val app = SpringApplication(BootstrapMain::class.java)
        app.setAdditionalProfiles(getProfile())
        app.run(*args)
    }


