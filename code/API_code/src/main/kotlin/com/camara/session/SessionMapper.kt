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

import com.camara.configuration.CamaraQoDConfiguration
import com.camara.model.SessionInfo
import com.camara.scef.FlowInfoMapper
import com.camara.scef.QosProfileEnum
import com.camara.scef.model.AsSessionWithQoSSubscription
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SessionMapper(
    val flowInfoMapper: FlowInfoMapper,
    val camaraQoDConfiguration: CamaraQoDConfiguration,
) {

    fun mapSessionInfoToAsSessionWithQoSSubscription(
        session: SessionInfo,
    ): AsSessionWithQoSSubscription {
        return AsSessionWithQoSSubscription()
            .flowInfo(flowInfoMapper.flowInfos(session))
            .supportedFeatures("0")
            .ueIpv4Addr(session.ueId.ipv4addr)
            .ueIpv6Addr(session.ueId.ipv6addr)
            .externalId(session.ueId.externalId)
            .msisdn(session.ueId.msisdn)
            .qosReference(QosProfileEnum.valueOf(session.qos.toString()).qosId)
            .notificationDestination(buildNotificationUrl(session, camaraQoDConfiguration.baseUrl()))
            .expirationTime(mapSecondsSinceEpochToIsoOffsetDateTime(session.duration.toLong()))
    }

    fun mapSecondsSinceEpochToIsoOffsetDateTime(durationInSeconds: Long) =
        ZonedDateTime.now(ZoneId.of("Europe/Paris"))
            .plusSeconds(durationInSeconds)
            .truncatedTo(ChronoUnit.SECONDS)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    fun buildNotificationUrl(session: SessionInfo, baseUrl: String): String {
        val odiSuffix = camaraQoDConfiguration.odiSuffix().orElse("")
        return "$baseUrl/${session.id}$odiSuffix"
    }

    companion object {
        const val NONE = "none"
        const val AUTH_TOKEN = "&authToken="
        const val ONE_DAY_IN_SECONDS = 86400
    }
}
