package com.wolf.inaction.web;

import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * curl localhost:4000/api/v1/token username=foo password=123
 * curl localhost:4000/api/v1/foo Authorization:'Bearer xxxxx'
 * Created on 2021/5/28 6:42 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class RouterVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RouterVerticle.class);

    WebClient webClient;
    JWTAuth jwtAuth;
    JWTAuthHandler jwtHandler;

    @Override
    public void start() throws Exception {
        webClient = WebClient.create(new Vertx(vertx));
        Router router = Router.router(vertx);

        router.route("/index").handler(request -> {
            request.response().end("INDEX SUCCESS");
        });

        BodyHandler bodyHandler = BodyHandler.create();// extracts http request body payloads
        // bodyHandler proces all put/post
        router.post().handler(bodyHandler);
        router.put().handler(bodyHandler);

        String prefix = "/api/v1";

        router.post(prefix + "/register").handler(this::register);
        router.post(prefix + "/token").handler(this::token);

        // jwtHandler
        String publicKey = CryptoHelper.publicKey();
        String privateKey = CryptoHelper.privateKey();

        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setBuffer(publicKey))
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setBuffer(privateKey)));

        jwtHandler = JWTAuthHandler.create(jwtAuth);

        router.get(prefix + "/:username/:year/:month")// can extract path parameters by prefixing elements with ":"
                .handler(jwtHandler)// handlers can be chained
                .handler(this::checkUser)
                .handler(this::monthlySteps);

        vertx.createHttpServer()
                .requestHandler(router)// a router is just anohter http request handler
                .listen(8080);
    }

    private void getTest(RoutingContext routingContext) {
        System.out.println(111);
    }

    private void register(RoutingContext ctx) {
        webClient
                .post(3000, "localhost", "/register")
                .putHeader("Content-Type", "application/json")
                .rxSendJson(ctx.getBodyAsJson())// converts the request from a buffer to jsonObject
                .subscribe(
                        response -> sendStatusCode(ctx, response.statusCode()),
                        err -> sendBadGateway(ctx, err)
                );
    }

    private void sendStatusCode(RoutingContext ctx, int code) {
        ctx.response().setStatusCode(code).end();
    }

    private void sendBadGateway(RoutingContext ctx, Throwable err) {
        logger.error("Woops", err);
        ctx.fail(502);
    }

    private void fetchUser(RoutingContext ctx) {
        webClient
                .get(3000, "localhost", "/" + ctx.pathParam("username"))// extracts a path parameter
                .as(BodyCodec.jsonObject())// convert the response to a jsonObject
                .rxSend()
                .subscribe(
                        resp -> forwardJsonOrStatusCode(ctx, resp),
                        err -> sendBadGateway(ctx, err)
                );
    }

    private void forwardJsonOrStatusCode(RoutingContext ctx, HttpResponse<JsonObject> resp) {
        if (resp.statusCode() != 200) {
            sendStatusCode(ctx, resp.statusCode());
        } else {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(resp.body().encode());// ends the response with some content
        }
    }

    private void checkUser(RoutingContext ctx) {
        String subject = ctx.user().principal().getString("sub");// user name from the jwt token
        if (!ctx.pathParam("username").equals(subject)) {
            sendStatusCode(ctx, 403);
        } else {
            ctx.next();// pass to the next handler
        }
    }

    private void monthlySteps(RoutingContext ctx) {
        String deviceId = ctx.user().principal().getString("deviceId");// from the jwt token
        String year = ctx.pathParam("year");
        String month = ctx.pathParam("month");
        webClient
                .get(3001, "localhost", "/" + deviceId + "/" + year + "/" + month)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .subscribe(
                        resp -> forwardJsonOrStatusCode(ctx, resp),
                        err -> sendBadGateway(ctx, err)
                );
    }

    private void token(RoutingContext ctx) {
        JsonObject payload = ctx.getBodyAsJson();// extract the credentials from the request to /api/v1/token
        String username = payload.getString("username");
        webClient
                .post(3000, "localhost", "/authenticate")
                .expect(ResponsePredicate.SC_SUCCESS)
                .rxSendJson(payload)
                // use flatMap to chain request
                .flatMap(resp -> fetchUserDetails(username))// on success, make another request
                .map(resp -> resp.body().getString("deviceId"))
                .map(deviceId -> makeJwtToken(username, deviceId))
                .subscribe(
                        token -> sendToken(ctx, token),
                        err -> handleAuthError(ctx, err)
                );
    }

    private void sendToken(RoutingContext ctx, String token) {
        ctx.response().putHeader("Content-Type", "application/jwt").end(token);
    }

    private void handleAuthError(RoutingContext ctx, Throwable err) {
        logger.error("Authentication error", err);
        ctx.fail(401);
    }

    private Single<HttpResponse<JsonObject>> fetchUserDetails(String username) {
        return webClient
                .get(3000, "localhost", "/" + username)
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .rxSend();
    }

    private String makeJwtToken(String username, String deviceId) {
        JsonObject claims = new JsonObject()// custom claims
                .put("deviceId", deviceId);
        JWTOptions jwtOptions = new JWTOptions()
                .setAlgorithm("RS256")
                .setExpiresInMinutes(10_080)// 7 days
                .setIssuer("10k-steps-api")// a claim that is in the jwt specification
                .setSubject(username);
        return jwtAuth.generateToken(claims, jwtOptions);
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(RouterVerticle.class.getName());
    }
}
