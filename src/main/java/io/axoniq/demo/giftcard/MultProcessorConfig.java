package io.axoniq.demo.giftcard;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.MultiStreamableMessageSource;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
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
    public EventStorageEngine storageEngine(Serializer defaultSerializer,
                                            @Qualifier("permanent") PersistenceExceptionResolver persistenceExceptionResolver,
                                            @Qualifier("eventSerializer") Serializer eventSerializer,
                                            AxonConfiguration configuration,
                                            @Qualifier("permanent") EntityManagerProvider entityManagerProvider,
                                            @Qualifier("permanent") TransactionManager transactionManager) {
        return JpaEventStorageEngine.builder()
                                    .snapshotSerializer(defaultSerializer)
                                    .upcasterChain(configuration.upcasterChain())
                                    .persistenceExceptionResolver(persistenceExceptionResolver)
                                    .eventSerializer(eventSerializer)
                                    .entityManagerProvider(entityManagerProvider)
                                    .transactionManager(transactionManager)
                                    .build();
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
        public EventStorageEngine ephemeralStorageEngine(Serializer defaultSerializer,
                                                         @Qualifier("ephemeral") PersistenceExceptionResolver persistenceExceptionResolver,
                                                         @Qualifier("eventSerializer") Serializer eventSerializer,
                                                         AxonConfiguration configuration,
                                                         @Qualifier("ephemeral") EntityManagerProvider entityManagerProvider,
                                                         @Qualifier("ephemeral") TransactionManager transactionManager) {
            return JpaEventStorageEngine.builder()
                                        .snapshotSerializer(defaultSerializer)
                                        .upcasterChain(configuration.upcasterChain())
                                        .persistenceExceptionResolver(persistenceExceptionResolver)
                                        .eventSerializer(eventSerializer)
                                        .entityManagerProvider(entityManagerProvider)
                                        .transactionManager(transactionManager)
                                        .build();
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
    public SagaStore sagaStore(@Qualifier("permanent") EntityManagerProvider entityManagerProvider,
                               Serializer defaultSerializer){
        return JpaSagaStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(defaultSerializer)
                .build();
    }

    @Bean TokenStore tokenStore(@Qualifier("permanent") EntityManagerProvider entityManagerProvider,
                                Serializer defaultSerializer){
        return JpaTokenStore.builder()
                            .serializer(defaultSerializer)
                            .entityManagerProvider(entityManagerProvider)
                            .build();
    }

    @Bean
    public MultiStreamableMessageSource multiStreamableMessageSource(@Qualifier("permanent") EmbeddedEventStore permanentEventStore,
                                                                     @Qualifier("ephemeral") EmbeddedEventStore ephemeralEventStore){

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
