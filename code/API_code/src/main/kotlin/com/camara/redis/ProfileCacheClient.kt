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

import com.camara.profile.ProfileCache
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.quarkus.redis.datasource.value.ReactiveValueCommands
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProfileCacheClient(val objectMapper: ObjectMapper, redisDs: ReactiveRedisDataSource) {

    val redisClient: ReactiveValueCommands<String, String> = redisDs.value(String::class.java, String::class.java)

    private fun readMapFromCache(): Uni<Map<String, ProfileCache>> = redisClient[MAP_OF_PROFILES]
        .map { profiles -> objectMapper.readValue<Map<String, ProfileCache>>(profiles ?: EMPTY_MAP) }

    fun addEntry(profileCache: ProfileCache): Uni<Void> {
        return readMapFromCache().flatMap { profileMap ->
            redisClient.set(
                MAP_OF_PROFILES,
                objectMapper.writeValueAsString(
                    profileMap.plus(Pair(profileCache.qosProfile.name, profileCache))
                )
            )
        }
    }

    fun readEntry(profileId: String): Uni<ProfileCache> {
        return readMapFromCache().flatMap { profileMap ->
            val profile = profileMap[profileId]
            if (profile != null) {
                Uni.createFrom().item(profile)
            } else {
                Uni.createFrom().failure(NoSuchElementException("Profile with id: $profileId was not found"))
            }
        }
    }

    fun readEntries(): Uni<List<ProfileCache>> {
        return readMapFromCache().map { profileMap -> profileMap.values.toList() }
    }

    fun deleteEntries(): Uni<String> {
        return redisClient.getdel(MAP_OF_PROFILES)
    }

    companion object {
        const val MAP_OF_PROFILES = "profiles"
        const val EMPTY_MAP = "{}"
    }
}
