package io.axoniq.demo.giftcard;

import io.axoniq.demo.giftcard.saga.SimpleAverageTracker;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintStream;

@RestController("/micrometer")
public class MicrometerRestController {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private SimpleAverageTracker simpleAverageTracker;

//    @GetMapping("/histogram")
//    public String getHistogram() {
//        return meterRegistry.get("Saga.timer.percentile").timer().takeSnapshot().percentileValues().toString();
//    }

    @GetMapping
    public String getSimpleTrackerValues() {
        return simpleAverageTracker.getValues().toString();
    }

}
