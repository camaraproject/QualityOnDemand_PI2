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
package com.camara.configuration

import io.smallrye.config.ConfigMapping
import java.util.Optional

@ConfigMapping(prefix = "camara.notifications")
@JvmDefaultWithCompatibility
interface CamaraQoDConfiguration {
    fun baseUrl(): String
    fun accessKey(): String
    fun odiSuffix(): Optional<String>
}

