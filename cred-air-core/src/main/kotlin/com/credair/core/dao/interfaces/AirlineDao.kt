package com.credair.core.dao.interfaces

import com.credair.core.model.Airline
import com.credair.common.dao.BaseDao

interface AirlineDao : BaseDao<Airline, Long> {
    fun findByCode(code: String): Airline?
    fun findByCountry(country: String): List<Airline>
    fun findActiveAirlines(): List<Airline>
}