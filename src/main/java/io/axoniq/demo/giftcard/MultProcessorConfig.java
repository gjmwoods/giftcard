package io.axoniq.demo.giftcard;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.axonframework.config.EventProcessingConfigurer;
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

import java.time.Duration;

@Configuration
public class MultProcessorConfig {

    @Autowired
    public void configureTimer(MeterRegistry meterRegistry){
        Timer.builder("Saga.timer")
             .maximumExpectedValue(Duration.ofMillis(200))
             .publishPercentiles(0.1, 0.5, 0.9, 0.95, 0.99) // median and 95th percentile
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
                                 .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                                 .build();
    }


    @Autowired
    public void configureSaga(EventProcessingConfigurer config){

        //configure the tracking config and eventStore sources for SagaExample
        config.registerSubscribingEventProcessor("SagaExampleProcessor");

    }
}
