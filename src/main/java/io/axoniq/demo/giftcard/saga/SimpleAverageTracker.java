package io.axoniq.demo.giftcard.saga;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SimpleAverageTracker {

    public SimpleAverageTracker(){
        this.currentAverage= new AtomicLong(0);
        this.number= new AtomicInteger(0);
        this.totalTime=new AtomicLong(0);
    }

    private AtomicLong currentAverage;
    private AtomicInteger number;
    private AtomicLong totalTime;

    public void updateAverage(Long newValue){
        number.incrementAndGet();
        totalTime.addAndGet(newValue);
        currentAverage.set(totalTime.get()/number.get());
    }

    public Long getCurrentAverage(){
        return currentAverage.get();
    }

}
