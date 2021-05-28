package com.wolf.inaction.beyondcallback

import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.predicate.ResponsePredicate
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class CollectorServiceWithCoroutine : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(CollectorServiceWithCoroutine::class.java)
    private lateinit var webClient: WebClient// lateinit indicate that the filed will not be initialized in a constructor

    override suspend fun start() {// 是suspend
        webClient = WebClient.create(vertx)
        vertx.createHttpServer()
            .requestHandler(this::handleRequest)
            .listen(8080).await()// wait for http server to be started
    }

    private suspend fun fetchTemperature(port: Int): JsonObject {
        return webClient
            .get(port, "localhost", "/")
            .expect(ResponsePredicate.SC_SUCCESS)
            .`as`(BodyCodec.jsonObject())
            .send().await()
            .body()
    }

    private suspend fun sendToSnapshot(json: JsonObject) {
        webClient
            .post(4000, "localhost", "/")
            .expect(ResponsePredicate.SC_SUCCESS)
            .sendJson(json).await()
    }

    private fun handleRequest(request: HttpServerRequest) {
        // 包围的目的，while the start method is suspending, the HTTP request handler is not a suspending function type,
        // and it will be called outside of a Kotlin coroutine context.
        // Calling launch ensures a coutine context is created, so suspending methods can be called
        launch {
            try {
                // fetching each temperature is asynchronous
                val t1 = async { fetchTemperature(3000) }
                val t2 = async { fetchTemperature(3001) }
                val t3 = async { fetchTemperature(3002) }

                val array = Json.array(t1.await(), t2.await(), t3.await())// wait for all values
                val json = json { obj("data" to array) }// DSL

                sendToSnapshot(json)
                request.response()
                    .putHeader("Content-Type", "application/json")
                    .end(json.encode())
            } catch (err: Throwable) {
                logger.error("Something went wrong", err)
                request.response().setStatusCode(500).end()
            }
        }
    }
}