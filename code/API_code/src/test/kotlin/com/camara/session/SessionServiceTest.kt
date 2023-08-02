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

import com.camara.TestUtils
import com.camara.TestUtils.createSessionInfo
import com.camara.TestUtils.initializeSessionToCreate
import com.camara.error.DurationOutOfBoundException
import com.camara.profile.ProfileCache
import com.camara.redis.ProfileCacheClient
import com.camara.redis.SessionCacheClient
import com.camara.stubForOauth
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import java.io.FileInputStream

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To be able to define @BeforeEach @AfterAll
internal class SessionServiceTest {

    @InjectMock
    lateinit var sessionCacheClient: SessionCacheClient

    @InjectMock
    lateinit var profileCacheClient: ProfileCacheClient

    @Inject
    lateinit var service: SessionService

    val wiremock: WireMockServer = WireMockServer(
        WireMockConfiguration.options().port(8888).notifier(ConsoleNotifier(true))
    )

    final val sessionToCreate = initializeSessionToCreate()
    final val session = createSessionInfo()
    final val id = session.sessionId
    final val appId = "appId"
    final val cacheId = "$appId:$id"
    val sessionCache = SessionCache(cacheId, "asSessionId", session)
    val json = FileInputStream("wiremock/__files/apigm-createSession.json")
        .readAllBytes()
        .decodeToString()

    @BeforeEach
    fun setup() {
        wiremock.start()
        wiremock.stubForOauth()
    }

    @AfterAll
    fun tearDown() {
        wiremock.stop()
    }

    @Test
    fun `createSession should return 201 on success`() {
        wiremock.stubFor(
            post(urlEqualTo("/apigm/subscriptions"))
                .willReturn(okJson(json))
        )
        `when`(profileCacheClient.readEntry(sessionToCreate.qosProfile)).thenReturn(
            Uni.createFrom().item(
                ProfileCache(1, TestUtils.createQoSProfile(sessionToCreate.qosProfile))
            )
        )

        `when`(sessionCacheClient.addEntry(sessionCache)).thenReturn(
            Uni.createFrom().voidItem()
        )

        val reply = service.createSession(sessionToCreate.duration(100), "xOapiApplicationId")
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .assertCompleted()
            .item

        assertThat(reply.applicationServer.ipv4Address).isEqualTo(session.applicationServer.ipv4Address)
    }

    @Test
    fun `deleteSession should not throw exception when session exists`() {
        wiremock.stubFor(
            delete(urlEqualTo("/apigm/subscriptions/asSessionId"))
                .willReturn(okJson(json))
        )

        `when`(sessionCacheClient.readEntry(cacheId))
            .thenReturn(Uni.createFrom().item(SessionCache("", "asSessionId", createSessionInfo())))

        `when`(sessionCacheClient.removeEntry(cacheId))
            .thenReturn(Uni.createFrom().item(SessionCache("", "asSessionId", createSessionInfo())))

        service.deleteSession(id, appId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .assertCompleted()
    }

    @Test
    fun `deleteSession should throw NoSuchElementException when session doesn't exist`() {
        `when`(sessionCacheClient.readEntry(cacheId))
            .thenReturn(Uni.createFrom().failure(NoSuchElementException("Not found")))

        service.deleteSession(id, appId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure()
            .assertFailedWith(NoSuchElementException::class.java, "Not found")
    }

    @Test
    fun `getSession should return existing session`() {
        `when`(sessionCacheClient.readEntry(cacheId)).thenReturn(Uni.createFrom().item(sessionCache))
        service.getSession(id, appId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .assertItem(session)
    }

    @Test
    fun `getSession should throw NotFoundException when session doesn't exist`() {
        `when`(sessionCacheClient.readEntry(cacheId)).thenReturn(
            Uni.createFrom().failure(NoSuchElementException("Session $cacheId was not found"))
        )

        service.getSession(id, appId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure()
            .assertFailedWith(NoSuchElementException::class.java, "Session $cacheId was not found")
    }

    @Test
    fun `createSession should return 400 when duration is smaller than qosProfile minDuration`() {
        `when`(profileCacheClient.readEntry(sessionToCreate.qosProfile)).thenReturn(
            Uni.createFrom().item(
                ProfileCache(1, TestUtils.createQoSProfile(sessionToCreate.qosProfile))
            )
        )
        service.createSession(sessionToCreate.duration(0), "xOapiApplicationId")
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(
                DurationOutOfBoundException::class.java,
                "Duration should be between 60 and 3600 for this profile"
            )
    }

    @Test
    fun `createSession should return 400 when duration is greater than qosProfile maxDuration`() {
        `when`(profileCacheClient.readEntry(sessionToCreate.qosProfile)).thenReturn(
            Uni.createFrom().item(
                ProfileCache(1, TestUtils.createQoSProfile(sessionToCreate.qosProfile))
            )
        )
        service.createSession(sessionToCreate.duration(100_000), "xOapiApplicationId")
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(
                DurationOutOfBoundException::class.java,
                "Duration should be between 60 and 3600 for this profile"
            )
    }
}
