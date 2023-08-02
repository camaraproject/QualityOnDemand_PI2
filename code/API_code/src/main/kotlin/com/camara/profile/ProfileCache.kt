package com.camara.profile

import com.camara.model.QosProfile

data class ProfileCache(
    val flowId: Int,
    val qosProfile: QosProfile,
)
