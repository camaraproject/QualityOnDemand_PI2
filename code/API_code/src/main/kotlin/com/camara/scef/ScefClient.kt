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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@RegisterRestClient
@RegisterProvider(OidcClientRequestReactiveFilter::class)
@Path("/subscriptions")
interface ScefClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun scsAsIdSubscriptionsPost(
        @Valid @NotNull asSessionWithQoSSubscription: AsSessionWithQoSSubscription,
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

