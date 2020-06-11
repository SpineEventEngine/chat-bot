package io.spine.chatbot;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.spine.chatbot.jackson.PubsubPushNotificationDeserializer;

import javax.inject.Singleton;

/**
 * Creates Micronaut context bean definitions.
 */
@Factory
public class BeanFactory {

    /** Registers Pubsub push notification Jackson deserializer. **/
    @Singleton
    @Bean
    public PubsubPushNotificationDeserializer pubsubDeserializer() {
        return new PubsubPushNotificationDeserializer();
    }
}
