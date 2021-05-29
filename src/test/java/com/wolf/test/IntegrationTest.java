package com.wolf.test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Description:
 * Created on 2021/5/29 9:29 AM
 *
 * @author 李超
 * @version 0.0.1
 */
@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)// test methods run in order
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integeration tests for the public API")
@Testcontainers// use testcontainers support
public class IntegrationTest {
    @Container
    private static final DockerComposeContainer CONTAINERS =
            new DockerComposeContainer(new File("../docker-compose.yml"));

    RequestSpecification requestSpecification;

    @BeforeAll
    public void prepareSpec() {
        requestSpecification = new RequestSpecBuilder()
                .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))// all request and response will be logged
                .setBaseUri("http://localhost:4000/")
                .setBasePath("/api/v1")
                .build();
    }

    private final HashMap<String, JsonObject> registrations = new HashMap<String, JsonObject>() {
        {
            put("Foo", new JsonObject()
                    .put("username", "Foo")
                    .put("password", "foo-123")
                    .put("email", "foo@email.me")
                    .put("city", "Lyon")
                    .put("deviceId", "a1b2c3")
                    .put("makePublic", true));
        }
    };

    private final HashMap<String, String> tokens = new HashMap<>();

    @Test
    @Order(1)
    @DisplayName("Register some users")
    void registerUsers() {
        registrations.forEach((key, registration) -> {
            given(requestSpecification)
                    .contentType(ContentType.JSON)
                    .body(registration.encode())// encode the json data to a string
                    .post("/register")// post to
                    .then()
                    .assertThat()
                    .statusCode(200);// assert that the status 200

            JsonObject login = new JsonObject()
                    .put("username", "key")
                    .put("password", registration.getString("password"));

            String token = given(requestSpecification)
                    .contentType(ContentType.JSON)
                    .body(login.encode())
                    .post("/token")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .contentType("application/jwt")// assert header in response
                    .extract()// extract the resp
                    .asString();

            assertThat(token)
                    .isNotNull()
                    .isNotBlank();

            tokens.put(key, token);

            JsonPath jsonPath = given(requestSpecification)
                    .headers("Authorization", "Bearer" + tokens.get("Foo"))// pass a JWT token
                    .get("/Foo/total")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .extract()
                    .jsonPath();

            assertThat(jsonPath.getInt("count")).isNotNull().isEqualTo(6255);
        });


    }


}
