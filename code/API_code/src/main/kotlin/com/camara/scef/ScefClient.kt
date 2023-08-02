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
package com.camara.scef

import com.camara.scef.model.AsSessionWithQoSSubscription
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter
import io.smallrye.mutiny.Uni
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
@RegisterProvider(OidcClientRequestReactiveFilter::class)
@Path("/subscriptions")
interface ScefClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun scsAsIdSubscriptionsPost(
        @Valid
        @NotNull asSessionWithQoSSubscription: AsSessionWithQoSSubscription,
    ): Uni<AsSessionWithQoSSubscription>

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun scsAsIdSubscriptionsGet(
        @PathParam("id") id: String,
    ): Uni<AsSessionWithQoSSubscription?>

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.WILDCARD)
    fun scsAsIdSubscriptionsDelete(
        @PathParam("id") id: String,
    ): Uni<Response>
}
