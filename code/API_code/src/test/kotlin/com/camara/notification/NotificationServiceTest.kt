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
import com.camara.redis.RedisCacheClient
import com.camara.session.SessionCache
import com.camara.stubForOauth
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To be able to define @BeforeEach @AfterAll
internal class NotificationServiceTest {

    @Inject
    lateinit var notificationService: NotificationService

    @InjectMock
    lateinit var redisCacheClient: RedisCacheClient

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
        val notification = createNotificationData().transaction(session.id.toString())
        val sessionCache = SessionCache(session.id, notification.transaction.toString(), session)
        `when`(redisCacheClient.readEntry(session.id)).thenReturn(
            Uni.createFrom().item(sessionCache)
        )

        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo("${session.notificationUri.path}/notifications"))
                .willReturn(WireMock.ok())
        )

        notificationService.notify(notification, session.id)
        verify(redisCacheClient, times(1))
            .readEntry(session.id)

        assertEquals(0, wiremock.findUnmatchedRequests().requests.size)

    }
}
