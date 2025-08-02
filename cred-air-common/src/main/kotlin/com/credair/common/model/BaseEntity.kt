package com.credair.common.model

import java.time.LocalDateTime

abstract class BaseEntity {
    abstract val id: Long?
    abstract val createdAt: LocalDateTime?
    abstract val updatedAt: LocalDateTime?
}