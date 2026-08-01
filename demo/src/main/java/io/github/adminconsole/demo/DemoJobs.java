package io.github.adminconsole.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component("demoJobs")
public class DemoJobs {
    private final AtomicInteger runs = new AtomicInteger();
    @Scheduled(cron = "0 0 0 1 1 *") public void refreshCache() { runs.incrementAndGet(); }
    public int getRuns() { return runs.get(); }
}
