import io.nats.client.JetStream
import io.nats.client.Nats
import io.nats.client.Options
import io.nats.client.api.PublishAck
import io.nats.client.impl.NatsMessage
import io.quarkus.mongodb.panache.kotlin.PanacheMongoCompanion
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.ConfigProvider

object NatsTestHelper {
    val jetStream: JetStream

    init {
        val natsConnectionOptions = with(ConfigProvider.getConfig()) {
            Options.Builder()
                .server(getConfigValue("quarkus.messaging.nats.servers").value)
                .userInfo(
                    getConfigValue("quarkus.messaging.nats.username").value,
                    getConfigValue("quarkus.messaging.nats.password").value,
                )
                .build()
        }
        jetStream = Nats.connect(natsConnectionOptions).jetStream()
    }

    inline fun <reified T : PanacheMongoEntity> publishMessage(payload: T, subject: String): PublishAck {
        return NatsMessage.builder()
            .subject(subject)
            .data(Json.encodeToString(payload))
            .build()
            .let { jetStream.publish(it) }
    }
}

@Serializable
data class NatsTestMessage(val testValue: String) : PanacheMongoEntity() {
    companion object : PanacheMongoCompanion<NatsTestMessage>
}
