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
import org.eclipse.microprofile.config.ConfigProvider
import java.util.UUID
import java.util.logging.Logger
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class RedisCacheClient(val redisDs: ReactiveRedisDataSource) {

    private val logger: Logger = Logger.getLogger(this.javaClass.canonicalName)

    private val host = ConfigProvider.getConfig()
        .getConfigValue("camara.kubernetes.config.host")
        .value

    private val password = ConfigProvider.getConfig()
        .getConfigValue("camara.kubernetes.config.password")
        .value

    init {
        logger.info("Connecting to redis host=${host.replaceBefore("@", "redis://xxx:xxx")}")
    }

    val redisClient: ReactiveValueCommands<UUID, SessionCache> =
        redisDs.value(UUID::class.java, SessionCache::class.java)

    fun addEntry(sessionCache: SessionCache) {
        redisClient.set(sessionCache.sessionInfoId, sessionCache)
            .subscribe()
            .with(
                {
                    logger.fine("Entry ${sessionCache.asSessionId} added")
                },
                {
                    logger.severe("Unable to add entry for ${sessionCache.asSessionId}")
                })
    }

    fun removeEntry(sessionId: UUID) {
        redisClient.getdel(sessionId)
            .subscribe()
            .with(
                {
                    logger.fine("Entry ${sessionId} deleted")
                },
                {
                    logger.severe("Unable to delete entry ${sessionId}")
                }
            )
    }

    fun readEntry(sessionId: UUID): Uni<SessionCache?> {
        return redisClient.get(sessionId)
    }
}
