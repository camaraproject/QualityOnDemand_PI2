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

import com.camara.model.CreateSession
import com.camara.model.SessionInfo
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestHeader
import java.net.URI
import java.util.UUID

@ApplicationScoped
@Path("/sessions")
class SessionController(
    val service: SessionService,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createSession(
        @Valid
        createSession: CreateSession,
        @RestHeader("X-OAPI-Application-Id")
        applicationId: String,
    ): Uni<Response> {
        checkParameters(createSession)
        return service.createSession(createSession, applicationId).map {
            Response.created(URI("sessions/${it.sessionId}"))
                .entity(it)
                .build()
        }
    }

    private fun checkParameters(createSession: CreateSession) {
        if (createSession.applicationServerPorts.ranges != null &&
            createSession.applicationServerPorts.ranges.isNotEmpty()
        ) {
            val nbOfRanges = createSession.applicationServerPorts.ranges.size
            val nbOfCompliantRanges = createSession.applicationServerPorts.ranges.filter { it.from < it.to }.size
            require(nbOfRanges == nbOfCompliantRanges) { FROM_MUST_BE_LOWER_THAN_TO }
        }
    }

    @DELETE
    @Path("/{sessionId}")
    fun deleteSession(
        @PathParam("sessionId") sessionId: UUID,
        @RestHeader("X-OAPI-Application-Id")
        applicationId: String,
    ): Uni<Void> {
        return service.deleteSession(sessionId, applicationId)
    }

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSession(
        @PathParam("sessionId") sessionId: UUID,
        @RestHeader("X-OAPI-Application-Id")
        applicationId: String,
    ): Uni<SessionInfo?> {
        return service.getSession(sessionId, applicationId)
    }

    companion object {
        const val FROM_MUST_BE_LOWER_THAN_TO = "from must be lower than to in port ranges"
    }
}
