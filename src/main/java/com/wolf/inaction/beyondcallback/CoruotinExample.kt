package com.wolf.inaction.beyondcallback

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// main is wrapped in runBlocking, because suspended method are being called,
// so the execution must wait for all coroutines to complete
fun main() = runBlocking {
    val job1 = launch { delay(500) }// start a job, execute in parallel

    fun fib(n: Long): Long = if (n < 2) n else fib(n - 1) + fib(n - 2)
    val job2 = async { fib(42) }// start a job that returns a value, async is for code blocks that return a value
    job1.join()// waits for the job to complete

    println("job1 has completed")
    println("job2 fib(42) = ${job2.await()}")// gets the value when the job completes
}