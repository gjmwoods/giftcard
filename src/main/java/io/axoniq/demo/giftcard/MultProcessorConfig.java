package io.axoniq.demo.giftcard;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.MultiStreamableMessageSource;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;


@Configuration
public class MultProcessorConfig {

    @Autowired
    public void configureTimer(MeterRegistry meterRegistry){
        Timer.builder("Saga.timer")
             .maximumExpectedValue(Duration.ofMillis(200))
             .publishPercentiles(0.1, 0.5, 0.95) // median and 95th percentile
             .publishPercentileHistogram()
             .register(meterRegistry);
    }

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
