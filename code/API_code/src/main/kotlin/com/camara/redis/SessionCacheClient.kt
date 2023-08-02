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
package com.camara.redis

import com.camara.session.SessionCache
import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.quarkus.redis.datasource.value.ReactiveValueCommands
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.logging.Logger

@ApplicationScoped
class SessionCacheClient(val redisDs: ReactiveRedisDataSource) {

    private val logger: Logger = Logger.getLogger(this.javaClass.canonicalName)

    val redisClient: ReactiveValueCommands<String, SessionCache> =
        redisDs.value(String::class.java, SessionCache::class.java)

    fun addEntry(sessionCache: SessionCache): Uni<Void> =
        redisClient.set(sessionCache.sessionInfoId, sessionCache)
            .onItem()
            .transform {
                logger.fine("Entry ${sessionCache.asSessionId} added")
                it
            }
            .onFailure()
            .transform {
                logger.severe("Unable to add entry for ${sessionCache.asSessionId}")
                it
            }

    fun removeEntry(sessionId: String): Uni<SessionCache> =
        redisClient.getdel(sessionId)
            .onFailure()
            .transform {
                logger.severe("Unable to delete entry $sessionId")
                it
            }
            .map {
                logger.fine("Entry $sessionId deleted")
                it
            }

    fun readEntry(sessionId: String): Uni<SessionCache> {
        return redisClient.get(sessionId).flatMap {
            if (it != null) {
                Uni.createFrom().item(it)
            } else {
                Uni.createFrom().failure(NoSuchElementException("Session $sessionId was not found"))
            }
        }
    }
}
