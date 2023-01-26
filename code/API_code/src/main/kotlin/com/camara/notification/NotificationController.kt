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
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@ApplicationScoped
@Path("/notifications")
class NotificationController(
    val service: NotificationService,
) {
    @POST
    @Consumes("application/json")
    @Path("/{id}")
    fun notification(
        @PathParam("id") id: UUID,
        @Valid @NotNull notification: NotificationData,
    ) {
        service.notify(notification, id)
    }
}
