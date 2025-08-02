package com.credair.common.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.db.DataSourceFactory
import javax.validation.Valid
import javax.validation.constraints.NotNull

class DatabaseConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    lateinit var database: DataSourceFactory
}