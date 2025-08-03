package com.credair.flight.search.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.credair.core.model.FlightSearchResult
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import java.time.Duration

class CacheConfig : AbstractModule() {

    override fun configure() {
    }

    @Provides
    @Singleton
    fun provideFlightSearchCache(): Cache<String, List<FlightSearchResult>> {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build()
    }
}