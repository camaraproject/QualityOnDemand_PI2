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
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.smallrye.mutiny.Uni
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import java.util.UUID
import javax.ws.rs.NotFoundException


@QuarkusTest
internal class SessionControllerTest {

    @InjectMock
    lateinit var sessionService: SessionService

    @Test
    fun createSession() {
        val sessionToCreate = initializeSessionToCreate()
        `when`(sessionService.createSession(sessionToCreate))
            .thenReturn(Uni.createFrom().item(createSessionInfo()))

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(sessionToCreate)
            .`when`().post("/sessions")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(containsStringIgnoringCase("\"ipv4addr\":\"192.168.0.1\""))
    }

    @Test
    fun `deleteSession should return 204 when session exist`() {
        val id = UUID.randomUUID()
        doNothing().`when`(sessionService).deleteSession(id)

        RestAssured
            .given()
            .`when`().delete("/sessions/$id")
            .then()
            .statusCode(204)
    }

    @Test
    fun `deleteSession should return 404 when session doesn't exist`() {
        val id = UUID.randomUUID()
        `when`(sessionService.deleteSession(id))
            .thenThrow(NotFoundException())

        RestAssured
            .given()
            .`when`()
            .delete("/sessions/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun `getSession should return existing session`() {
        val expected = createSessionInfo()
        `when`(sessionService.getSession(expected.id))
            .thenReturn(Uni.createFrom().item(expected))

        RestAssured
            .given()
            .`when`()
            .get("/sessions/${expected.id}")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(containsStringIgnoringCase("\"ipv4addr\":\"192.168.0.1\""))
    }

    @Test
    fun `getSession should return 404 when session doesn't exist`() {
        val id = UUID.randomUUID()
        `when`(sessionService.getSession(id))
            .thenThrow(NotFoundException())

        RestAssured
            .given()
            .`when`()
            .get("/sessions/$id")
            .then()
            .statusCode(404)
    }
}
