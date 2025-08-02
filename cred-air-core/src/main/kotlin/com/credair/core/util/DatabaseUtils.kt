package com.credair.core.util

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import java.sql.DriverManager
import javax.sql.DataSource

object DatabaseUtils {
    fun createDataSource(config: DatabaseConfig): DataSource {
        return SimpleDataSource(config.url, config.username, config.password)
    }
    
    fun createJdbi(dataSource: DataSource): Jdbi {
        return Jdbi.create(dataSource)
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
    }
}

interface DatabaseConfig {
    val url: String
    val driver: String
    val username: String
    val password: String
}

class SimpleDataSource(
    private val url: String,
    private val username: String,
    private val password: String
) : DataSource {
    override fun getConnection() = DriverManager.getConnection(url, username, password)
    override fun getConnection(username: String?, password: String?) = DriverManager.getConnection(url, username, password)
    override fun <T : Any?> unwrap(iface: Class<T>?): T = throw UnsupportedOperationException()
    override fun isWrapperFor(iface: Class<*>?) = false
    override fun setLogWriter(out: java.io.PrintWriter?) = throw UnsupportedOperationException()
    override fun getLogWriter() = throw UnsupportedOperationException()
    override fun setLoginTimeout(seconds: Int) = throw UnsupportedOperationException()
    override fun getLoginTimeout() = 0
    override fun getParentLogger() = throw UnsupportedOperationException()
}