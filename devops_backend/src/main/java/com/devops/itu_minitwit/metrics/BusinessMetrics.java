package com.devops.itu_minitwit.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.devops.itu_minitwit.repository.UserRepository;
import com.devops.itu_minitwit.repository.MessageRepository;
import com.devops.itu_minitwit.repository.FollowerRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class BusinessMetrics {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FollowerRepository followerRepository;
    private final MeterRegistry registry;

    private final AtomicLong totalUsers = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalFollows = new AtomicLong(0);
    private final AtomicLong unflaggedMessages = new AtomicLong(0);

    // Store follower counts for top 10 users
    private final Map<String, AtomicLong> topFollowerCounts = new ConcurrentHashMap<>();

    public BusinessMetrics(MeterRegistry registry,
                           UserRepository userRepository,
                           MessageRepository messageRepository,
                           FollowerRepository followerRepository) {
        this.registry = registry;
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

        updateTopFollowers();
    }

    private void updateTopFollowers() {
        // Reset all existing counts to 0 (handles users dropping out of top 10)
        topFollowerCounts.values().forEach(count -> count.set(0));

        List<Object[]> topUsers = followerRepository.findTop10ByFollowerCount();

        for (Object[] row : topUsers) {
            String username = (String) row[0];
            Long followerCount = (Long) row[1];

            // Get or create the AtomicLong for this user
            AtomicLong counter = topFollowerCounts.computeIfAbsent(username, name -> {
                AtomicLong newCounter = new AtomicLong(0);
                Gauge.builder("minitwit_top_followers", newCounter, AtomicLong::get)
                     .description("Follower count for top users")
                     .tag("username", name)
                     .register(registry);
                return newCounter;
            });

            counter.set(followerCount);
        }
    }
}