package com.camara.profile

import com.camara.model.QosProfile
import com.camara.redis.ProfileCacheClient
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProfileService(val profileCacheClient: ProfileCacheClient) {

    fun getProfile(name: String): Uni<QosProfile> = profileCacheClient
        .readEntry(name)
        .map { it.qosProfile }

    fun getProfiles(): Uni<List<QosProfile>> = profileCacheClient
        .readEntries()
        .map { profileCaches -> profileCaches.map { profileCache -> profileCache.qosProfile } }

    fun putProfile(profileCache: ProfileCache) = profileCacheClient.addEntry(profileCache)

    fun getProfileCaches(): Uni<List<ProfileCache>> = profileCacheClient.readEntries()
    fun deleteProfileCaches(): Uni<String> = profileCacheClient.deleteEntries()
}
