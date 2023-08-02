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

import com.camara.scef.model.NotificationData
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam

@ApplicationScoped
@Path("/notifications")
class NotificationController(
    val service: NotificationService,
) {
    @POST
    @Consumes("application/json")
    @Path("/{id}")
    fun notification(
        @PathParam("id") id: String,
        @Valid @NotNull notification: NotificationData,
    ) {
        service.notify(notification, id)
    }
}
