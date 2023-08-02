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

import com.camara.model.EventDetail
import com.camara.model.EventQosStatus
import com.camara.model.QosStatusChangedEvent
import com.camara.scef.model.Event
import com.camara.scef.model.NotificationData
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class NotificationMapper {

    fun mapToStatus(event: Event): EventQosStatus {
        return when (event) {
            Event.SESSION_TERMINATION -> EventQosStatus.UNAVAILABLE
            Event.SUCCESSFUL_RESOURCES_ALLOCATION -> EventQosStatus.AVAILABLE
            else -> EventQosStatus.UNAVAILABLE
        }
    }

    fun mapToNotification(data: NotificationData, id: UUID): com.camara.model.Event {
        return QosStatusChangedEvent().apply {
            eventDetail = EventDetail().apply {
                sessionId = id
                qosStatus = data
                    .eventReports
                    .filter {
                        it.event == Event.SUCCESSFUL_RESOURCES_ALLOCATION || it.event == Event.SESSION_TERMINATION
                    }
                    .map {
                        mapToStatus(it.event)
                    }
                    .first()
            }
        }
    }
}
