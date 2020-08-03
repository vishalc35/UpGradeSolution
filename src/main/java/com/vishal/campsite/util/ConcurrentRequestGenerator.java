package com.vishal.campsite.util;

import com.vishal.campsite.model.Reserve;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentRequestGenerator {

    private int numPoolThreads;
    private int numParallelReqs;
    private Reserve reserve;
    private String serviceUri;

    public ConcurrentRequestGenerator() {
    }

    public ConcurrentRequestGenerator(int numPoolThreads, int numParallelReqs, Reserve reserve, String serviceUri) {
        this.numPoolThreads = numPoolThreads;
        this.numParallelReqs = numParallelReqs;
        this.reserve = reserve;
        this.serviceUri = serviceUri;
    }

    public void run() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(numPoolThreads);
        List<RequestThread> threads = new ArrayList<RequestThread>();
        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i<numParallelReqs; i++) {
            RequestThread requestThread = new RequestThread(reserve, serviceUri);
            es.execute((Runnable) requestThread);
            threads.add(requestThread);
        }
        es.shutdown();
        es.awaitTermination(2, TimeUnit.MINUTES);

        // Delete created resources
        for (RequestThread rt: threads) {
            if (rt.getResourceUri() != null)
                restTemplate.delete(rt.getResourceUri());
        }
    }
}
