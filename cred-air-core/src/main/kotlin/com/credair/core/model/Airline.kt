package com.credair.core.model

import com.credair.common.model.BaseEntity
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Airline(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("code")
    val code: String,

    @JsonProperty("country")
    val country: String,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("website")
    val website: String? = null,

    @JsonProperty("active")
    val active: Boolean = true,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime? = null,

    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime? = null
)