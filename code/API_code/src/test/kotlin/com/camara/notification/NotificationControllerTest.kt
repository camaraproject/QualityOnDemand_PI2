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
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.doNothing
import java.util.UUID

@QuarkusTest
internal class NotificationControllerTest {

    @InjectMock
    lateinit var notificationService: NotificationService

    @Test
    fun notifyTest() {
        val sessionId = UUID.randomUUID()
        val session = TestUtils.createSessionInfo(sessionId)
        val notification = createNotificationData(sessionId)
        doNothing().`when`(notificationService).notify(notification, "appId:$sessionId")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(notification)
            .`when`()
            .post(("/notifications/appId:${session.sessionId}"))
            .then()
            .statusCode(204)

        verify(notificationService, times(1))
            .notify(notification, "appId:${session.sessionId}")
    }
}
