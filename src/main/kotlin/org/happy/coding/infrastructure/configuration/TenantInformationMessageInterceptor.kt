package org.happy.coding.infrastructure.configuration

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InterceptorBinding
import jakarta.interceptor.InvocationContext
import org.happy.coding.infrastructure.TenantInformationHolder
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@InterceptorBinding
@Retention(RUNTIME)
@Target(CLASS, FUNCTION)
annotation class TenantInformationInterceptorBinding

@TenantInformationInterceptorBinding
@Interceptor
class TenantInformationMessageInterceptor(
    private val tenantInformationHolder: TenantInformationHolder,
) {

    @AroundInvoke
    @Suppress("unused")
    fun intercept(context: InvocationContext): Any {
        tenantInformationHolder.setTenant(topic = (context.parameters.first { it is SubscribeMessage<*> } as SubscribeMessage<*>).subject)
        return context.proceed()
    }
}