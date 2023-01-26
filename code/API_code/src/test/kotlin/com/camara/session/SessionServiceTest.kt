/*
* Software Name : camara-qod-api
* Version: 0.1.0
* SPDX-FileCopyrightText: Copyright (c) 2022 Orange
* SPDX-License-Identifier: Apache-2.0
*
* This software is distributed under the Apache-2.0,
* the text of which is available at https://www.apache.org/licenses/LICENSE-2.0
* or see the "LICENCE" file for more details.
*
* Author: patrice.conil@orange.com
*/
package com.camara.session

import com.camara.TestUtils.createSessionInfo
import com.camara.TestUtils.initializeSessionToCreate
import com.camara.redis.RedisCacheClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import java.io.FileInputStream
import javax.inject.Inject
import javax.ws.rs.NotFoundException


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To be able to define @BeforeEach @AfterAll
internal class SessionServiceTest {

    @Inject
    lateinit var service: SessionService

    @InjectMock
    lateinit var redisClient: RedisCacheClient

    val wiremock: WireMockServer = WireMockServer(
        WireMockConfiguration.options().port(8888).notifier(ConsoleNotifier(true))
    )

    final val sessionToCreate = initializeSessionToCreate()
    final val session = createSessionInfo()
    val id = session.id
    val sessionCache = SessionCache(id, "asSessionId", session)
    val json = FileInputStream("wiremock/__files/apigm-createSession.json")
        .readAllBytes()
        .decodeToString()

    @BeforeEach
    fun setup() {
        wiremock.start()
        wiremock.stubFor(
            post(urlEqualTo("/oauth/v3/token"))
                .willReturn(
                    okJson(
                        "{\n" +
                                "\"access_token\":\"mF_9.B5f-4.1JqM\",\n" +
                                "\"token_type\":\"Bearer\",\n" +
                                "\"expires_in\":3600,\n" +
                                "\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"\n" +
                                "}".trimIndent()
                    )
                )
        )
    }

    @AfterAll
    fun tearDown() {
        wiremock.stop()
    }

    @Test
    fun createSession() {
        wiremock.stubFor(
            post(urlEqualTo("/apigm/subscriptions"))
                .willReturn(okJson(json))
        )
        doNothing().`when`(redisClient).addEntry(sessionCache)

        val reply = service.createSession(sessionToCreate).subscribe()
            .withSubscriber(UniAssertSubscriber.create())

        assertEquals(session.asId.ipv4addr, reply.awaitItem().assertCompleted().item.asId.ipv4addr)
    }

    @Test
    fun `deleteSession should not throw exception when session exist`() {
        wiremock.stubFor(
            delete(urlEqualTo("/apigm/subscriptions/asSessionId"))
                .willReturn(okJson(json))
        )
        doNothing().`when`(redisClient).removeEntry(id)
        service.deleteSession(id)
    }

    @Test
    fun `deleteSession should throw NotFounException when session doesn't exist`() {
        `when`(redisClient.removeEntry(id)).thenThrow(NotFoundException())
        val myException = assertThrows(
            NotFoundException::class.java,
            { service.deleteSession(id) },
            "Expected deleteSession() to throw, but it didn't"
        )
        assertEquals(NotFoundException::class.java, myException.javaClass)
    }

    @Test
    fun `getSession should return existing session`() {
        `when`(redisClient.readEntry(id)).thenReturn(Uni.createFrom().item(sessionCache))
        val reply = service.getSession(id).await().indefinitely()
        assertEquals(session, reply)
    }

    @Test
    fun `getSession should throw NotFounException when session doesn't exist`() {
        `when`(redisClient.readEntry(id)).thenThrow(NotFoundException())
        val myException = assertThrows(
            NotFoundException::class.java,
            { service.getSession(id) },
            "Expected getSession() to throw, but it didn't"
        )
        assertEquals(NotFoundException::class.java, myException.javaClass)
    }

}
