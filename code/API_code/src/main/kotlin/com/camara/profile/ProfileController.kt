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

import com.camara.model.QosProfile
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@ApplicationScoped
@Path("/qosProfiles")
class ProfileController(val profileService: ProfileService) {
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProfile(name: String): Uni<QosProfile> {
        return profileService.getProfile(name)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getProfiles(): Uni<List<QosProfile>> {
        return profileService.getProfiles()
    }
}
