package com.rentloop.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MongoKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(MongoKeepAliveService.class);

    private final MongoTemplate mongoTemplate;

    public MongoKeepAliveService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Sends a lightweight { ping: 1 } command to MongoDB Atlas every 5 minutes.
     * This keeps the connection active and prevents MongoDB Atlas free tier from
     * pausing due to inactivity.
     */
    @Scheduled(fixedRate = 300000, initialDelay = 15000)
    public void keepAlivePing() {
        try {
            Document result = mongoTemplate.executeCommand(new Document("ping", 1));
            log.info("MongoDB Atlas Keep-Alive Ping successful: ok={}", result.get("ok"));
        } catch (Exception e) {
            log.warn("MongoDB Atlas Keep-Alive Ping warning: {}", e.getMessage());
        }
    }
}
