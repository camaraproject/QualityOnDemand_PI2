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

import com.camara.TestUtils.createSessionInfo
import com.camara.configuration.CamaraQoDConfiguration
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import javax.inject.Inject

@QuarkusTest
internal class SessionMapperTest {

    @Inject
    lateinit var sessionMapper: SessionMapper

    @Inject
    lateinit var camaraQoDConfiguration: CamaraQoDConfiguration

    @Test
    fun `buildNotificationUrl should return an uri equal to baseUrl+sessionId`() {
        val session = createSessionInfo().apply {
            notificationUri = URI("https://mybaseUrl")
        }
        val suffix = camaraQoDConfiguration.odiSuffix().orElse("")
        val url = sessionMapper.buildNotificationUrl(session, "http://mybaseUrl")
        assertEquals("http://mybaseUrl/${session.id}$suffix", url)
    }

    @Test
    fun `expirationDate must not contain Z for zulu time but an offset`() {
        println(sessionMapper.mapSecondsSinceEpochToIsoOffsetDateTime(60))

    }
}
