package com.credair.core.util

import java.sql.DriverManager
import javax.sql.DataSource

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