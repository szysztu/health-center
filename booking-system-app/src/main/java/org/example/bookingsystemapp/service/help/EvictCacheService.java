package org.example.bookingsystemapp.service.help;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class EvictCacheService {
    @CacheEvict(value = "freeSchedules", key = "#doctorId")
    public void evictFreeSchedules(Long doctorId) {
    }
}
