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
import com.camara.model.SessionInfo
import com.camara.profile.ProfileCache
import com.camara.scef.model.FlowInfo
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FlowInfoMapper {

    fun flowInfos(session: SessionInfo, profile: ProfileCache): List<FlowInfo> {
        return extractPortsPairs(extractPortList(session.devicePorts), extractPortList(session.applicationServerPorts))
            .map { portPair ->
                FlowInfo()
                    .flowId(profile.flowId)
                    .flowDescriptions(createFlowDescriptions(portPair, session))
            }
    }

    fun <T> extractPortsPairs(c1: Iterable<T>, c2: Iterable<T>): List<Pair<T, T>> {
        return c1.flatMap { c1Item -> c2.map { c2Item -> c1Item to c2Item } }
    }

    fun extractPortList(portsSpec: PortsSpec): List<Int> {
        return portsSpec.ranges.orEmpty()
            .flatMap { range -> range.from.rangeTo(range.to) }
            .toMutableList()
            .plus(portsSpec.ports.orEmpty())
            .distinct()
            .sorted()
    }

    private fun createFlowDescriptions(portsPair: Pair<Int, Int>, session: SessionInfo) =
        createFlowDescription(
            FlowParam(
                session.device.ipv4Address.publicAddress,
                portsPair.first.toString(),
                session.applicationServer.ipv4Address,
                portsPair.second.toString()
            )
        )

    private fun createFlowDescription(flowParam: FlowParam) = mutableListOf(
        "permit in $PROTOCOL from ${flowParam.ueIp} ${flowParam.uePort} to ${flowParam.asIp} ${flowParam.asPort}",
        "permit out $PROTOCOL from ${flowParam.asIp} ${flowParam.asPort} to ${flowParam.ueIp} ${flowParam.uePort}"
    )

    companion object {
        const val PROTOCOL = 17 // Todo SCEF doesn't support ip as protocol
    }
}
