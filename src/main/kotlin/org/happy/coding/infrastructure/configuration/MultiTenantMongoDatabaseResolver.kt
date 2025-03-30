package org.happy.coding.infrastructure.configuration

import io.quarkus.mongodb.panache.common.MongoDatabaseResolver
import jakarta.enterprise.context.RequestScoped
import org.happy.coding.infrastructure.TenantInformation

@RequestScoped
class MultiTenantMongoDatabaseResolver(private val tenantInformation: TenantInformation) : MongoDatabaseResolver {
    override fun resolve(): String = tenantInformation.tenant
}
