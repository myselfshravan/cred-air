package com.credair.common.util

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import javax.sql.DataSource

object DatabaseUtils {
    fun createJdbi(dataSource: DataSource): Jdbi {
        return Jdbi.create(dataSource)
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
    }
}