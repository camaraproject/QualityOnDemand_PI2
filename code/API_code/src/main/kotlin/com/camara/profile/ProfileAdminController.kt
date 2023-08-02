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
package com.camara.profile

import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Valid
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI

@ApplicationScoped
@Path("/admin/profiles")
class ProfileAdminController(val profileService: ProfileService) {

    @PUT
    @Path("/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun putProfile(profileId: String, @Valid profileCache: ProfileCache): Uni<RestResponse<Unit>> {
        return profileService.putProfile(profileCache).map {
            RestResponse.created(URI.create("/profiles/$profileId"))
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getProfiles(): Uni<List<ProfileCache>> {
        return profileService.getProfileCaches()
    }

    @DELETE
    @Path("/{profileId}")
    @Produces(MediaType.TEXT_PLAIN)
    fun deleteProfiles(profileId: String): Uni<String> {
        return if (profileId == "deleteAll") {
            profileService.deleteProfileCaches().map { "Profiles deleted" }
        } else {
            Uni.createFrom().item("Single delete not supported...profiles unchanged")
        }
    }
}
