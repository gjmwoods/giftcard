package io.axoniq.demo.giftcard.rest;

import io.axoniq.demo.giftcard.saga.SimpleAverageTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("metrics")
public class MetricRestController {

    @Autowired
    private SimpleAverageTracker averageTracker;

    @GetMapping(path="getmetrics")
    public String getExperimentMetrics(){
        return averageTracker.getValues().toString();
    }

}