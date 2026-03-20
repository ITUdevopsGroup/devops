package com.devops.itu_minitwit.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devops.itu_minitwit.repository.UserRepository;
import com.devops.itu_minitwit.repository.MessageRepository;
import com.devops.itu_minitwit.repository.FollowerRepository;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BusinessMetrics {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FollowerRepository followerRepository;

    private final AtomicLong totalUsers = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalFollows = new AtomicLong(0);
    private final AtomicLong unflaggedMessages = new AtomicLong(0);

    public BusinessMetrics(MeterRegistry registry,
                           UserRepository userRepository,
                           MessageRepository messageRepository,
                           FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.followerRepository = followerRepository;

        Gauge.builder("minitwit_users_total", totalUsers, AtomicLong::get)
             .description("Total number of registered users")
             .register(registry);

        Gauge.builder("minitwit_messages_total", totalMessages, AtomicLong::get)
             .description("Total number of messages")
             .register(registry);

        Gauge.builder("minitwit_follows_total", totalFollows, AtomicLong::get)
             .description("Total number of follow relationships")
             .register(registry);

        Gauge.builder("minitwit_messages_unflagged", unflaggedMessages, AtomicLong::get)
             .description("Total number of visible (unflagged) messages")
             .register(registry);
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void updateMetrics() {
        totalUsers.set(userRepository.count());
        totalMessages.set(messageRepository.count());
        totalFollows.set(followerRepository.count());
        unflaggedMessages.set(messageRepository.countByFlagged(0));
    }
}