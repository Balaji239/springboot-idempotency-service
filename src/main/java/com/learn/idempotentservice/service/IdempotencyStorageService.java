package com.learn.idempotentservice.service;

import com.learn.idempotentservice.dto.IdempotentResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyStorageService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyStorageService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean claimLock(String key, long ttl, TimeUnit timeUnit) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                formatKey(key),
                serialize(new IdempotentResponse(0, null, "IN_PROGRESS")),
                Duration.of(ttl, timeUnit.toChronoUnit())
        );
        return Boolean.TRUE.equals(success);
    }

    public IdempotentResponse getRecord(String key) {
        String rawJson = redisTemplate.opsForValue().get(formatKey(key));
        if (rawJson == null) return null;
        return deserialize(rawJson);
    }

    public void saveResponse(String key, int statusCode, String body, long ttl, TimeUnit timeUnit) {

        IdempotentResponse response = new IdempotentResponse(statusCode, body, "COMPLETED");

        redisTemplate.opsForValue().set(
                formatKey(key),
                serialize(response),
                Duration.of(ttl, timeUnit.toChronoUnit())
        );
    }

    public void deleteKey(String key) {
        redisTemplate.delete(formatKey(key));
    }

    public String formatKey(String key) {
        return "idempotency:" + key;
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failure", e);
        }
    }

    private IdempotentResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, IdempotentResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failure", e);
        }
    }

}
