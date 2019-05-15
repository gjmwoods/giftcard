package io.axoniq.demo.giftcard;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventsourcing.MultiStreamableMessageSource;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RegisterDefaultEntities(packages = {
        "org.axonframework.eventsourcing.eventstore.jpa"
})
public class MultProcessorConfig {

    @Primary
    @Qualifier("permanent")
    @Bean
    public AxonServerEventStore permanentEventStore(AxonServerConnectionManager connectionManager) {
        return AxonServerEventStore.builder()
                                   .configuration(AxonServerConfiguration.builder()
                                   .context("permanent").build())
                                   .platformConnectionManager(connectionManager)
                                   .build();
    }

    @Bean
    @Qualifier("ephemeral")
    public AxonServerEventStore ephemeralEventStore(AxonServerConnectionManager connectionManager) {
        return AxonServerEventStore.builder()
                                   .configuration(AxonServerConfiguration.builder()
                                                                         .context("ephemeral").build())
                                   .platformConnectionManager(connectionManager)
                                   .build();
    }

    @Bean
    public MultiStreamableMessageSource multiStreamableMessageSource(@Qualifier("permanent") AxonServerEventStore permanentEventStore, @Qualifier("ephemeral") AxonServerEventStore ephemeralEventStore){

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
