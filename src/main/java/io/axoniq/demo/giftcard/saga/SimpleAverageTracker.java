package io.axoniq.demo.giftcard.saga;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SimpleAverageTracker {

    private AtomicLong currentAverage;
    private AtomicInteger number;
    private AtomicLong totalTime;
    private ArrayList<Long> values;

    public SimpleAverageTracker(){
        this.currentAverage= new AtomicLong(0);
        this.number= new AtomicInteger(0);
        this.totalTime=new AtomicLong(0);
        this.values= new ArrayList<>();
    }

    public void updateAverage(Long newValue){
        values.add(newValue);
        number.incrementAndGet();
        totalTime.addAndGet(newValue);
        currentAverage.set(totalTime.get()/number.get());
    }

    public Long getCurrentAverage(){
        return currentAverage.get();
    }

    public ArrayList<Long> getValues(){
        return values;
    }

}