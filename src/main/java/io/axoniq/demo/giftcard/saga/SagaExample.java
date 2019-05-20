package io.axoniq.demo.giftcard.saga;

import io.axoniq.demo.giftcard.api.BackgroundCheckFinished;
import io.axoniq.demo.giftcard.api.BackgroundCheckStarted;
import io.axoniq.demo.giftcard.api.IssuedEvt;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.TimeUnit;

@Saga
public class SagaExample {

    private static final Logger logger = LoggerFactory.getLogger(SagaExample.class);

    @Autowired
    private transient SimpleAverageTracker averageTracker;

    @Autowired
    private transient MeterRegistry meterRegistry;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    @Qualifier("ephemeral")
    private transient EmbeddedEventStore ephemeralES;

    @SagaEventHandler(associationProperty = "id")
    @StartSaga
    public void on(IssuedEvt event){
        Long now = System.currentTimeMillis();

        logger.debug("New IssuedEvent received. Starting background check");

        long processTime = now-event.getTimestamp();
        meterRegistry.timer("Saga.timer").record(processTime, TimeUnit.MILLISECONDS);
        logger.info("{}", processTime);
        ephemeralES.publish(GenericDomainEventMessage.asEventMessage(new BackgroundCheckStarted(event.getId(),now)));
    }

    @SagaEventHandler(associationProperty = "id")
    public void on(BackgroundCheckStarted event) throws InterruptedException{
        logger.debug("Background check started.");

        //do something that may takes a while...
        ephemeralES.publish(GenericDomainEventMessage.asEventMessage(new BackgroundCheckFinished(event.getId(),System.currentTimeMillis())));
    }

    @SagaEventHandler(associationProperty = "id")
    @EndSaga
    public void on(BackgroundCheckFinished event) {
        logger.debug("Background check came back fine");
    }

}
