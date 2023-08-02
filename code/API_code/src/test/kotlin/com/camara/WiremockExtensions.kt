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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

fun WireMockServer.stubForOauth() = this.stubFor(
    WireMock.post(WireMock.urlEqualTo("/oauth/v3/token"))
        .willReturn(
            WireMock.okJson(
                "{\n\"access_token\":\"mF_9.B5f-4.1JqM\",\n\"token_type\":\"Bearer\",\n\"expires_in\":3600,\n\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"\n${"}".trimIndent()}"
            )
        )
)
