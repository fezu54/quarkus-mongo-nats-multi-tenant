package org.happy.coding.infrastructure

import jakarta.enterprise.context.RequestScoped

private const val SINGLE_TENANT = "SINGLE_TENANT"

@RequestScoped
class TenantInformationHolder : TenantInformation {

    private var customerId: String = SINGLE_TENANT
    private var instanceId: String? = null

    override val tenant: String
        get() = instanceId?.let { "${customerId}_$it" } ?: customerId

    fun setTenant(topic: String) {
        val topicCharSequence = topic.split(".")
        customerId = topicCharSequence[1]
        instanceId = topicCharSequence[2]
    }
}

interface TenantInformation {
    val tenant: String
}