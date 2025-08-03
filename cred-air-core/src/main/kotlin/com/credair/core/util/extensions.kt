package com.credair.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.ws.rs.container.AsyncResponse

private val coroutineScope = CoroutineScope(Dispatchers.Default)

fun AsyncResponse.with(block: suspend () -> Any): AsyncResponse {
    val x = this
    coroutineScope.launch {
        val result = block.invoke()
        if (x.isSuspended) {
            x.resume(result)
        } else {
            println("[${Thread.currentThread().name}] AsyncResponse was already resumed or timed out. Result not sent.")
        }
    }
    return x
}