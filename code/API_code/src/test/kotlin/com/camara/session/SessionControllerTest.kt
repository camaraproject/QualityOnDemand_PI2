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
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.NotFoundException
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import java.util.UUID

@QuarkusTest
@Suppress("CascadingCallWrapping")
internal class SessionControllerTest {

    @InjectMock
    lateinit var sessionService: SessionService

    val appId = "appId"

    @Test
    fun createSession() {
        val sessionToCreate = initializeSessionToCreate()
        `when`(sessionService.createSession(sessionToCreate, appId))
            .thenReturn(
                Uni.createFrom().item(createSessionInfo())
            )

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .contentType(ContentType.JSON)
            .header("X-OAPI-Application-Id", appId)
            .body(sessionToCreate)
            .log()
            .all()
            .`when`()
            .post("/sessions")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(containsStringIgnoringCase("\"ipv4Address\":\"192.168.0.1\""))
    }

    @Test
    fun `deleteSession should return 204 when session exist`() {
        val id = UUID.randomUUID()
        `when`(sessionService.deleteSession(id, appId)).thenReturn(
            Uni.createFrom().voidItem()
        )

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .`when`()
            .delete("/sessions/$id")
            .then()
            .statusCode(204)
    }

    @Test
    fun `deleteSession should return 404 when session doesn't exist`() {
        val id = UUID.randomUUID()
        `when`(sessionService.deleteSession(id, appId))
            .thenThrow(NotFoundException())

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .`when`()
            .delete("/sessions/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun `getSession should return existing session`() {
        val expected = createSessionInfo()
        `when`(sessionService.getSession(expected.sessionId, appId))
            .thenReturn(Uni.createFrom().item(expected))

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .`when`()
            .get("/sessions/${expected.sessionId}")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(containsStringIgnoringCase("\"ipv4Address\":\"192.168.0.1\""))
    }

    @Test
    fun `getSession should return 404 when session doesn't exist`() {
        val sessionId = UUID.randomUUID()
        `when`(sessionService.getSession(sessionId, appId))
            .thenThrow(NotFoundException())

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .`when`()
            .get("/sessions/$sessionId")
            .then()
            .statusCode(404)
    }

    @Test
    fun `getSession should return 400 when request is malformed`() {
        val sessionToCreate = initializeSessionToCreate()
            .apply {
                device.phoneNumber = ""
            }

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .contentType(ContentType.JSON)
            .body(sessionToCreate)
            .`when`()
            .post("/sessions")
            .then()
            .statusCode(400)
            .body("message", CoreMatchers.containsString("phoneNumber"))
    }

    @Test
    fun `getSession should return 401 when authentication is missing`() {
        val sessionToCreate = initializeSessionToCreate()
            .apply {
                device.phoneNumber = ""
            }

        RestAssured
            .given()
            .header("X-OAPI-Application-Id", appId)
            .contentType(ContentType.JSON)
            .body(sessionToCreate)
            .`when`()
            .post("/sessions")
            .then()
            .statusCode(401)
    }

    @Test
    fun `getSession should return 403 when user has not admin or od role`() {
        val sessionToCreate = initializeSessionToCreate()
            .apply {
                device.phoneNumber = ""
            }

        RestAssured
            .given()
            .auth().preemptive().basic("user", "password")
            .header("X-OAPI-Application-Id", appId)
            .contentType(ContentType.JSON)
            .body(sessionToCreate)
            .`when`()
            .post("/sessions")
            .then()
            .statusCode(403)
    }

    @Test
    fun `getSession should return 405 when request call unsupported method`() {
        val sessionToCreate = initializeSessionToCreate()

        RestAssured
            .given()
            .auth().preemptive().basic("od", "password")
            .header("X-OAPI-Application-Id", appId)
            .contentType(ContentType.JSON)
            .body(sessionToCreate)
            .`when`()
            .put("/sessions")
            .then()
            .statusCode(405)
            .body("message", CoreMatchers.containsString("unsupported"))
    }
}
