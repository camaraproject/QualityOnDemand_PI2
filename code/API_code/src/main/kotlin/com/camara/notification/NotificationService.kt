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

import com.camara.api.SessionNotificationsCallbackApi
import com.camara.redis.SessionCacheClient
import com.camara.scef.model.NotificationData
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.RestClientBuilder
import java.util.UUID

@ApplicationScoped
class NotificationService(
    val redis: SessionCacheClient,
    val mapper: NotificationMapper,
) {
    fun notify(notification: NotificationData, id: String) {
        // Because original UUID was prefixed by "X-OAPI-Application-Id:"
        val uuid = UUID.fromString(id.substringAfter(":"))
        redis.readEntry(id).subscribe().with {
            it?.sessionInfo?.webhook?.notificationUrl?.let { uri ->
                // Todo manage notificationAuthToken
                RestClientBuilder
                    .newBuilder()
                    .baseUri(uri)
                    .build(SessionNotificationsCallbackApi::class.java)
                    .postNotification(mapper.mapToNotification(notification, uuid))
            }
        }
    }
}
