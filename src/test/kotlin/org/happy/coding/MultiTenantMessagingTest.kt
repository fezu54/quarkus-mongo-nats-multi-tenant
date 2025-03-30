package org.happy.coding

import NatsTestHelper.publishMessage
import NatsTestMessage
import com.mongodb.client.MongoClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import org.happy.coding.infrastructure.configuration.TenantInformationInterceptorBinding
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

@QuarkusTest
class MultiTenantMessagingTest {

    @Inject
    private lateinit var mongoClient: MongoClient

    @Test
    fun `Should store message payload in different databases when they are received from different tenants`() {
        // GIVEN
        val callers = mapOf(
            "customer-1_instance-1" to "test.customer-1.instance-1.test-event",
            "customer-2_instance-1" to "test.customer-2.instance-1.test-event",
            "customer-3_instance-1" to "test.customer-3.instance-1.test-event",
            "customer-4_instance-1" to "test.customer-4.instance-1.test-event",
            "customer-4_instance-2" to "test.customer-4.instance-2.test-event",
        )

        // WHEN
        runBlocking {
            callers.map { (key, subject) -> launch { publishMessage(NatsTestMessage(key), subject) } }
        }

        // THEN
        awaitReceiving(numberOfCalls = callers.size)

        val databaseNames = mongoClient.listDatabaseNames().toList()
        TestController.queue.iterator().forEach { (tenant, message) ->
            assertContains(databaseNames, tenant)
            mongoClient
                .getDatabase(tenant)
                .getCollection("NatsTestMessage", NatsTestMessage::class.java)
                .find()
                .toList()
                .also { documents ->
                    assertEquals(1, documents.size)
                    assertEquals(message.payload, documents.first())
                }
        }


    }

    private fun awaitReceiving(numberOfCalls: Int) = runBlocking {
        withTimeout(2.toDuration(SECONDS)) {
            var size = 0
            while (size != numberOfCalls) {
                size = TestController.queue.size
            }
        }
    }
}


@ApplicationScoped
class TestController {

    companion object {
        val queue = ConcurrentLinkedQueue<Pair<String, Message<NatsTestMessage>>>()
    }

    @TenantInformationInterceptorBinding
    @Incoming("test")
    suspend fun onMessageReceived(message: Message<NatsTestMessage>) {
        message.payload.persist()
        queue.offer(message.payload.testValue to message)
    }
}