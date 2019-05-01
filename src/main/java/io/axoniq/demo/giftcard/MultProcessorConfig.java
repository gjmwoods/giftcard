package io.axoniq.demo.giftcard;

import io.axoniq.demo.giftcard.query.CardSummaryProjection;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.MultiStreamableMessageSource;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.sql.SQLException;
import java.util.HashMap;
import javax.sql.DataSource;

@Configuration
@RegisterDefaultEntities(packages = {
        "org.axonframework.eventsourcing.eventstore.jpa"
})
public class MultProcessorConfig {

    @Primary
    @Qualifier("permanent")
    @Bean
    public EmbeddedEventStore permanentEventStore(
            @Qualifier("permanent") EventStorageEngine storageEngine, AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                                 .storageEngine(storageEngine)
                                 .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                                 .build();
    }


    @Primary
    @Qualifier("permanent")
    @Bean
    public JpaEventStorageEngine eventStorageEngine(Serializer eventSerializer,
                                                    Serializer snapshotSerializer,
                                                    DataSource dataSource,
                                                    EntityManagerProvider entityManagerProvider,
                                                    TransactionManager transactionManager) throws SQLException {
        return JpaEventStorageEngine.builder()
                                    .eventSerializer(eventSerializer)
                                    .snapshotSerializer(snapshotSerializer)
                                    .dataSource(dataSource)
                                    .entityManagerProvider(entityManagerProvider)
                                    .transactionManager(transactionManager)
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
    public MultiStreamableMessageSource multiStreamableMessageSource(@Qualifier("permanent") EmbeddedEventStore permanentEventStore, @Qualifier("ephemeral") EmbeddedEventStore ephemeralEventStore){

        HashMap<String, StreamableMessageSource<TrackedEventMessage<?>>> messageSources = new HashMap<>();

        messageSources.put("permanent", permanentEventStore);
        messageSources.put("ephemeral", ephemeralEventStore);

        return new MultiStreamableMessageSource(messageSources);
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
