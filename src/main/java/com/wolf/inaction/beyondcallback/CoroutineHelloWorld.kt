package com.wolf.inaction.beyondcallback

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.*

suspend fun hello(): String {// this function can be suspended
    delay(1000)// this function is suspending and will not block the caller thread
    return "hello!"
}

fun main() {
    runBlocking {// allows waiting for coroutines code to complete
        println(hello())
    }
}
// the call delay does not block the caller thread beacause that method can be suspended.
