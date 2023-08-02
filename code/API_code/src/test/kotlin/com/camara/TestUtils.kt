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

import com.camara.model.ApplicationServer
import com.camara.model.CreateSession
import com.camara.model.CreateSessionWebhook
import com.camara.model.Device
import com.camara.model.DeviceIpv4Addr
import com.camara.model.Duration
import com.camara.model.PortsSpec
import com.camara.model.QosProfile
import com.camara.model.Rate
import com.camara.model.RateUnitEnum
import com.camara.model.SessionInfo
import com.camara.model.TimeUnitEnum
import com.camara.scef.model.Event
import com.camara.scef.model.EventReport
import com.camara.scef.model.NotificationData
import com.camara.session.SessionCache
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
        .applicationServer(ApplicationServer().ipv4Address("192.168.0.1"))
        .applicationServerPorts(PortsSpec().ports(mutableListOf(5010)))
        .duration(60)
        .webhook(
            CreateSessionWebhook()
                .notificationAuthToken("TOKEN_1234567891234567890")
                .notificationUrl(URI.create("http://localhost:8888"))
        )
        .qosProfile("b55e2cc8-b386-4d90-9f95-b98ba20be050")
        .device(Device().ipv4Address(DeviceIpv4Addr().publicAddress("192.168.0.2")))
        .devicePorts(PortsSpec().ports(mutableListOf(5022)))

    fun createSessionInfo(uuid: UUID? = null): SessionInfo {
        val startedAt = Instant.now().epochSecond
        val createSession = initializeSessionToCreate()
        return SessionInfo()
            .sessionId(uuid ?: UUID.randomUUID())
            .applicationServer(createSession.applicationServer)
            .applicationServerPorts(createSession.applicationServerPorts)
            .duration(createSession.duration)
            .startedAt(startedAt)
            .expiresAt(startedAt + createSession.duration)
            .webhook(createSession.webhook)
            .qosProfile(createSession.qosProfile)
            .device(createSession.device)
            .devicePorts(createSession.devicePorts)
    }

    fun createQoSProfile(name: String): QosProfile {
        return QosProfile()
            .name(name)
            .description("Test Profile")
            .maxDownstreamRate(
                Rate()
                    .unit(RateUnitEnum.MBPS)
                    .value(20)
            )
            .maxDuration(
                Duration()
                    .unit(TimeUnitEnum.HOURS)
                    .value(1)
            )
            .minDuration(
                Duration()
                    .unit(TimeUnitEnum.SECONDS)
                    .value(60)
            )
    }

    fun createSessionCache(id: UUID): SessionCache {
        return SessionCache(id.toString(), "asSessionId", createSessionInfo(id))
    }
}
