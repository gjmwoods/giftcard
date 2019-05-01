package io.axoniq.demo.giftcard;

import io.axoniq.demo.giftcard.query.CardSummaryProjection;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.MultiStreamableMessageSource;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;

@Configuration
public class MultProcessorConfig {

    @Bean
    @Primary
    @Qualifier("permanent")
    public EventStorageEngine configureEventStoreForPermanentEvents(){
        return new InMemoryEventStorageEngine();
    }

    @Bean
    @Qualifier("permanent")
    public EmbeddedEventStore eventStore(EventStorageEngine storageEngine, AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                                 .storageEngine(storageEngine)
                                 .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                                 .build();
    }


    @Autowired
    public void configureSaga(EventProcessingConfigurer config){

        //configure the tracking config and eventStore sources for SagaExample
        config.registerSubscribingEventProcessor("SagaExampleProcessor");

    }
}
