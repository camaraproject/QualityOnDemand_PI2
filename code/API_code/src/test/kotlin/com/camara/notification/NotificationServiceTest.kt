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

package com.camara.notification

import com.camara.TestUtils
import com.camara.TestUtils.createNotificationData
import com.camara.redis.SessionCacheClient
import com.camara.session.SessionCache
import com.camara.stubForOauth
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To be able to define @BeforeEach @AfterAll
internal class NotificationServiceTest {

    @Inject
    lateinit var notificationService: NotificationService

    @InjectMock
    lateinit var sessionCacheClient: SessionCacheClient

    val wiremock: WireMockServer = WireMockServer(
        WireMockConfiguration.options().port(8888).notifier(ConsoleNotifier(true))
    )

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
    fun notifyTest() {
        val session = TestUtils.createSessionInfo()
        val sessionId = "appId:${session.sessionId}"
        val notification = createNotificationData().transaction(session.sessionId.toString())
        val sessionCache = SessionCache(sessionId, notification.transaction.toString(), session)
        `when`(sessionCacheClient.readEntry(sessionId)).thenReturn(
            Uni.createFrom().item(sessionCache)
        )

        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo("${session.webhook?.notificationUrl?.path}/notifications"))
                .willReturn(WireMock.ok())
        )

        notificationService.notify(notification, sessionId)
        verify(sessionCacheClient, times(1))
            .readEntry(sessionId)

        assertThat(wiremock.findUnmatchedRequests().requests.size).isEqualTo(0)
    }
}
