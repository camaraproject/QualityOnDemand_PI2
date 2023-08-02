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

import com.camara.error.DurationOutOfBoundException
import com.camara.error.InternalServerError
import com.camara.error.UnknownProfileException
import com.camara.model.CreateSession
import com.camara.model.SessionInfo
import com.camara.model.TimeUnitEnum
import com.camara.profile.ProfileCache
import com.camara.redis.ProfileCacheClient
import com.camara.redis.SessionCacheClient
import com.camara.scef.ScefClient
import com.camara.scef.id
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.resteasy.reactive.client.api.WebClientApplicationException
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class SessionService(
    @RestClient val client: ScefClient,
    val sessionMapper: SessionMapper,
    val sessionCacheClient: SessionCacheClient,
    val profileCacheClient: ProfileCacheClient,
) {
    private fun checkDuration(session: SessionInfo, profileCache: ProfileCache) {
        var duration = profileCache.qosProfile.minDuration
        val min = duration.value * unitToSeconds.getOrDefault(duration.unit, 1)
        duration = profileCache.qosProfile.maxDuration
        val max = duration.value * unitToSeconds.getOrDefault(duration.unit, 1)

        if (session.duration < min || session.duration > max) {
            throw DurationOutOfBoundException(
                "INVALID_ARGUMENT",
                "Duration should be between $min and $max for this profile"
            )
        }
    }

    fun createSession(createSession: CreateSession, applicationId: String): Uni<SessionInfo> {
        val startedAt = Instant.now().epochSecond
        val session = SessionInfo()
            .sessionId(UUID.randomUUID())
            .applicationServer(createSession.applicationServer)
            .applicationServerPorts(createSession.applicationServerPorts)
            .duration(createSession.duration)
            .startedAt(startedAt)
            .expiresAt(startedAt + createSession.duration)
            .webhook(createSession.webhook)
            .qosProfile(createSession.qosProfile)
            .device(createSession.device)
            .devicePorts(createSession.devicePorts)

        return profileCacheClient.readEntry(createSession.qosProfile).flatMap { profileCache ->
            checkDuration(session, profileCache)
            client.scsAsIdSubscriptionsPost(
                sessionMapper.mapSessionInfoToAsSessionWithQoSSubscription(
                    session,
                    profileCache
                )
            ).flatMap {
                sessionCacheClient.addEntry(
                    SessionCache("$applicationId:${session.sessionId}", it.id()!!, session)
                ).map { session }
            }.onFailure().recoverWithUni { err ->
                when (err) {
                    is WebClientApplicationException, is WebApplicationException -> Uni.createFrom().failure(err)

                    is NoSuchElementException -> Uni.createFrom().failure(
                        UnknownProfileException(
                            "INVALID_PROFILE",
                            err.message ?: "QoS Profile ${createSession.qosProfile} was not found"
                        )
                    )

                    else -> Uni.createFrom().failure(
                        WebApplicationException(
                            InternalServerError("500", "Unsupported error ${err.message}")
                        )
                    )
                }
            }
        }
    }

    fun deleteSession(sessionId: UUID, applicationId: String): Uni<Void> {
        // We remove entry in cache after session was deleted on scef side
        val cacheSessionId = "$applicationId:$sessionId"
        return sessionCacheClient.readEntry(cacheSessionId).map { sessionCache ->
            client.scsAsIdSubscriptionsDelete(sessionCache.asSessionId)
        }.flatMap {
            sessionCacheClient.removeEntry(cacheSessionId)
                .flatMap { Uni.createFrom().voidItem() }
                .onFailure()
                .transform { InternalServerError("FAILED_TO_REMOVE_SESSION", "Unable to delete session") }
        }
    }

    fun getSession(sessionId: UUID, applicationId: String): Uni<SessionInfo?> =
        sessionCacheClient.readEntry("$applicationId:$sessionId").flatMap { sessionCache ->
            if (sessionCache != null) {
                Uni.createFrom().item(sessionCache.sessionInfo)
            } else {
                Uni.createFrom().failure(NotFoundException("Session with id : $sessionId was not found"))
            }
        }

    companion object {
        val unitToSeconds = mapOf(
            TimeUnitEnum.SECONDS to 1,
            TimeUnitEnum.MINUTES to 60,
            TimeUnitEnum.HOURS to 3_600,
            TimeUnitEnum.DAYS to 86_400
        )
    }
}
