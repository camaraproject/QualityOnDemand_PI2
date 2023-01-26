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
package com.camara

import com.camara.model.AsId
import com.camara.model.CreateSession
import com.camara.model.PortsSpec
import com.camara.model.QosProfile
import com.camara.model.SessionInfo
import com.camara.model.UeId
import com.camara.scef.model.Event
import com.camara.scef.model.EventReport
import com.camara.scef.model.NotificationData
import java.net.URI
import java.time.Instant
import java.util.UUID

object TestUtils {
    fun createNotificationData(uuid: UUID? = null): NotificationData {
        return NotificationData()
            .transaction(URI.create("http://localhost:8080/$uuid").toASCIIString())
            .eventReports(
                listOf(
                    EventReport().event(Event.SESSION_TERMINATION)
                )
            )
    }

    fun initializeSessionToCreate() = CreateSession()
        .asId(AsId().ipv4addr("192.168.0.1"))
        .asPorts(PortsSpec().ports(mutableListOf(5010)))
        .duration(86400)
        .notificationAuthToken("token")
        .notificationUri(URI.create("http://localhost:8888"))
        .qos(QosProfile.L)
        .ueId(UeId().ipv4addr("192.168.0.2"))
        .uePorts(PortsSpec().ports(mutableListOf(5022)))

    fun createSessionInfo(uuid: UUID? = null): SessionInfo {
        val startedAt = Instant.now().epochSecond
        val createSession = initializeSessionToCreate()
        return SessionInfo()
            .id(uuid ?: UUID.randomUUID())
            .asId(createSession.asId)
            .asPorts(createSession.asPorts)
            .duration(createSession.duration)
            .startedAt(startedAt)
            .expiresAt(startedAt + createSession.duration)
            .notificationAuthToken(createSession.notificationAuthToken)
            .notificationUri(createSession.notificationUri)
            .qos(createSession.qos)
            .ueId(createSession.ueId)
            .uePorts(createSession.uePorts)

    }
}
