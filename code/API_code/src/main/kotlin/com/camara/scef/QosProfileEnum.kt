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

package com.camara.scef

enum class QosProfileEnum(val qosId: String) {
    QOS_E(""),
    QOS_L("173047be-d805-4df9-8cd7-9cb0b78cbc2b"),
    QOS_M("c96c6152-d400-4b84-9b1f-aed6c8ce4a15"),
    QOS_S("b55e2cc8-b386-4d90-9f95-b98ba20be050");

    companion object {
        private val map = values().associateBy(QosProfileEnum::qosId)
        operator fun get(qosId: String) = map[qosId]
    }
}
