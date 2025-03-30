package org.happy.coding

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import org.happy.coding.infrastructure.TenantInformationHolder
import org.happy.coding.infrastructure.configuration.TenantInformationMessageInterceptor
import kotlin.test.Test

class TenantInformationTest {
    @Test
    fun `Only multi tenant request interceptor is allowed to write tenant information`() {
        Konsist
            .scopeFromProduction()
            .properties()
            .filter { it.type?.name == TenantInformationHolder::class.simpleName }
            .map { property ->
                property.containingDeclaration.assertTrue { (it as KoClassDeclaration).name == TenantInformationMessageInterceptor::class.simpleName }
            }
    }

}