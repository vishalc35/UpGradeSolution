package com.vishal.campsite.util;

import com.vishal.campsite.model.Reserve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class RequestThread {

    private Reserve reserve;
    private String serviceUri;
    private URI resourceUri;

    private static final Logger logger = LoggerFactory.getLogger(RequestThread.class);

    public RequestThread() {
    }

    public RequestThread(Reserve reserve, String serviceUri) {
        this.reserve = reserve;
        this.serviceUri = serviceUri;
    }

    
    public void run() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Reserve> requestEntity = new HttpEntity<Reserve>(reserve, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            URI reserveUri = restTemplate.postForLocation(serviceUri, requestEntity);
            if (reserveUri == null) {
                logger.error(Thread.currentThread().getName() + " Cannot reserve campsite ");
            } else {
                resourceUri = reserveUri;
                logger.info(Thread.currentThread().getName() + " Successfully reserved campsite! Uri => " + reserveUri);
            }
        } catch (Exception e) {
            logger.error("Problem reserving campsite");
        }
    }

    public URI getResourceUri() {
        return resourceUri;
    }
}
