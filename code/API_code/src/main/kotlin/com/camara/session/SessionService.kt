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
package com.camara.session

import com.camara.model.CreateSession
import com.camara.model.SessionInfo
import com.camara.redis.RedisCacheClient
import com.camara.scef.ScefClient
import com.camara.scef.id
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.Instant
import java.util.UUID
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SessionService(
    @RestClient val client: ScefClient,
    val sessionMapper: SessionMapper,
    val redisCacheClient: RedisCacheClient,
) {

    fun createSession(createSession: CreateSession): Uni<SessionInfo> {
        val startedAt = Instant.now().epochSecond
        val session = SessionInfo()
            .id(UUID.randomUUID())
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

        return client.scsAsIdSubscriptionsPost(
            sessionMapper.mapSessionInfoToAsSessionWithQoSSubscription(session)
        ).map {
            redisCacheClient.addEntry(SessionCache(session.id, it.id()!!, session))
            session
        }

    }

    fun deleteSession(sessionId: UUID) {
        redisCacheClient.removeEntry(sessionId)
    }


    fun getSession(sessionId: UUID): Uni<SessionInfo?> {
        return redisCacheClient.readEntry(sessionId).map {
            it?.sessionInfo
        }
    }
}
