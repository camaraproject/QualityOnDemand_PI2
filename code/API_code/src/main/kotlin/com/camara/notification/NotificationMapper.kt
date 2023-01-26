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

import com.camara.model.EventReport
import com.camara.model.Notification
import com.camara.model.QosStatus
import com.camara.model.SessionEventType
import com.camara.scef.model.Event
import com.camara.scef.model.NotificationData
import java.util.UUID
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class NotificationMapper {

    fun mapToStatus(event: Event): QosStatus {
        return when (event) {
            Event.SESSION_TERMINATION -> QosStatus.UNAVAILABLE
            Event.SUCCESSFUL_RESOURCES_ALLOCATION -> QosStatus.AVAILABLE
            else -> QosStatus.REQUESTED
        }
    }

    fun mapToNotification(data: NotificationData, id: UUID): Notification {
        return Notification().apply {
            sessionId = id
            eventReports = data
                .eventReports
                .filter {
                    it.event == Event.SUCCESSFUL_RESOURCES_ALLOCATION ||
                            it.event == Event.SESSION_TERMINATION
                }
                .map {
                    EventReport()
                        .qosStatus(mapToStatus(it.event))
                        .eventType(SessionEventType.QOS_STATUS_CHANGED)
                }
        }
    }
}
