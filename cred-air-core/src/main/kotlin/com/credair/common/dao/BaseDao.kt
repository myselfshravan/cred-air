package com.credair.common.dao

interface BaseDao<T, ID> {
    fun save(entity: T): T
    fun update(entity: T): T
    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun delete(id: ID): Boolean
}