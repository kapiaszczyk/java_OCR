package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.util.HashingService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KeyService {

    private final RedisCommands<String, String> redisCommands;

    private final RedisClient redisKeyClient;
    private final HashingService hashingService;

    @Autowired
    public KeyService(@Qualifier("redisKeyClient") RedisClient redisKeyClient, HashingService hashingService) {
        this.redisKeyClient = redisKeyClient;
        this.redisCommands = redisKeyClient.connect().sync();
        this.hashingService = new HashingService();
    }

    public boolean isKeyValid(String key) {
        getAllKeys();
        return redisCommands.exists(key) == 1;
    }

    public String addKey(String key) {
        String value = LocalDateTime.now().toString();
        redisCommands.set(key, value);
        return key;
    }

    public String generateKey(PricingPlan pricingPlan) {
        String salt = LocalDateTime.now().toString();
        String key = pricingPlan.name() + "-" + hashingService.hash(salt);
        String hashedKey = hashingService.hashKey(key);
        if (duplicateKeyCheck(hashedKey)) {
            hashedKey = generateKey(pricingPlan);
        }
        addKey(hashedKey);
        return key;
    }

    public void getAllKeys() {
        System.out.println(redisCommands.keys("*"));
    }

    private boolean duplicateKeyCheck(String key) {
        return redisCommands.exists(key) == 1;
    }

}