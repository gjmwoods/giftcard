package io.axoniq.demo.giftcard;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventsourcing.MultiStreamableMessageSource;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


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

    @Bean
    @Qualifier("ephemeral")
    public EventStorageEngine configureEventStoreForEphemeralEvents(){
        return new InMemoryEventStorageEngine();
    }

    @Bean
    @Qualifier("ephemeral")
    public EmbeddedEventStore ephemeralEventStore(
            @Qualifier("ephemeral") EventStorageEngine storageEngine, AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                                 .storageEngine(storageEngine)
                                 .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                                 .build();
    }

    @Bean
    public MultiStreamableMessageSource multiStreamableMessageSource(@Qualifier("eventStore") EmbeddedEventStore permanentEventStore, @Qualifier("ephemeral") EmbeddedEventStore ephemeralEventStore){

        return MultiStreamableMessageSource.builder()
                                           .addMessageSource("permanent", permanentEventStore)
                                           .addMessageSource("ephemeral", ephemeralEventStore)
                                           .build();
    }

    @Autowired
    public void configureSaga(EventProcessingConfigurer config, MultiStreamableMessageSource multiStreamableMessageSource){

        //configure the tracking config and eventStore sources for SagaExample
        config.registerTrackingEventProcessor("SagaExampleProcessor", c-> multiStreamableMessageSource);

        //Needed if you want to change the name of the TrackingProcessor backing the Saga
//        config.assignHandlerTypesMatching("SagaExampleProcessor", SagaExample.class::equals);
    }

    //Sample duel Tracking Event Processor Configuration
//    @Autowired
//    public void configureTrackingProcessor(EventProcessingConfigurer config, MultiStreamableMessageSource multiStreamableMessageSource){
//
//        config.registerTrackingEventProcessor(CardSummaryProjection.class.getPackage().getName(), c -> multiStreamableMessageSource);
//    }

}
