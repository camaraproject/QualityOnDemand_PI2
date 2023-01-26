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

import com.camara.model.PortsSpec
import com.camara.model.QosProfile
import com.camara.model.SessionInfo
import com.camara.scef.model.FlowInfo
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class FlowInfoMapper {

    //Todo call SCEF to retrieve flow ids ?
    private val qosProfileEnumToFlowId: Map<QosProfile, Int> = mapOf(
        QosProfile.E to FLOW_ID_E,
        QosProfile.L to FLOW_ID_1,
        QosProfile.M to FLOW_ID_1,
        QosProfile.S to FLOW_ID_17
    )

    fun flowInfos(session: SessionInfo): List<FlowInfo> {
        return extractPortsPairs(extractPortList(session.uePorts), extractPortList(session.asPorts))
            .map { portPair ->
                FlowInfo()
                    .flowId(qosProfileEnumToFlowId[session.qos]!!)
                    .flowDescriptions(createFlowDescriptions(portPair, session))
            }
    }

    fun <T> extractPortsPairs(c1: Iterable<T>, c2: Iterable<T>): List<Pair<T, T>> {
        return c1.flatMap { c1Item -> c2.map { c2Item -> c1Item to c2Item } }
    }

    fun extractPortList(portsSpec: PortsSpec): List<Int> {
        return portsSpec.ranges.orEmpty()
            .flatMap { range -> range.from.rangeTo(range.to) }.toMutableList()
            .plus(portsSpec.ports.orEmpty()).distinct().sorted()
    }

    private fun createFlowDescriptions(portsPair: Pair<Int, Int>, session: SessionInfo) =
        createFlowDescription(
            FlowParam(
                session.ueId.ipv4addr,
                portsPair.first.toString(),
                session.asId.ipv4addr,
                portsPair.second.toString()
            )
        )

    private fun createFlowDescription(flowParam: FlowParam) = mutableListOf(
        "permit in $PROTOCOL from ${flowParam.ueIp} ${flowParam.uePort} " +
                "to ${flowParam.asIp} ${flowParam.asPort}",
        "permit out $PROTOCOL from ${flowParam.asIp} ${flowParam.asPort} " +
                "to ${flowParam.ueIp} ${flowParam.uePort}"
    )

    companion object {
        const val FLOW_ID_1 = 1
        const val FLOW_ID_17 = 17
        const val FLOW_ID_E = 0
        const val PROTOCOL = 17 //Todo SCEF doesn't support ip as protocol
    }
}

