package com.credair.airline.model

import com.credair.common.model.BaseEntity
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Airline(
    @JsonProperty("id")
    override val id: Long? = null,
    
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
    override val createdAt: LocalDateTime? = null,
    
    @JsonProperty("updated_at")
    override val updatedAt: LocalDateTime? = null
) : BaseEntity()