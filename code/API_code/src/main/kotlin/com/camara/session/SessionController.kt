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
import java.net.URI
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@ApplicationScoped
@Path("/sessions")
class SessionController(
    val service: SessionService,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createSession(@Valid createSession: CreateSession): Uni<Response> {
        checkParameters(createSession)
        return service.createSession(createSession).map {
            Response.created(URI("sessions/${it.id}"))
                .entity(it)
                .build()
        }
    }

    private fun checkParameters(createSession: CreateSession) {
        if (createSession.asPorts.ranges != null && createSession.asPorts.ranges.isNotEmpty()) {
            val nbOfRanges = createSession.asPorts.ranges.size
            if (nbOfRanges != createSession.asPorts.ranges.filter { it.from < it.to }.size)
                throw IllegalArgumentException(FROM_MUST_BE_LOWER_THAN_TO)
        }
    }


    @DELETE
    @Path("/{sessionId}")
    fun deleteSession(@PathParam("sessionId") sessionId: UUID) {
        service.deleteSession(sessionId)
    }

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSession(@PathParam("sessionId") sessionId: UUID): Uni<SessionInfo?> {
        return service.getSession(sessionId)
    }

    companion object {
        const val FROM_MUST_BE_LOWER_THAN_TO = "from must be lower than to in port ranges"

    }
}
