# CAMARA QoD API by Orange

This project implements CAMARA QoD API in front of SCEF.

The SCEF can be emulated by wiremock.
Minimal wiremock configuration is provided in wiremock directory.
You can download wiremock from the official website [here](https://wiremock.org/).

Note: As we use redis to cache sessions, you need to have a running docker env to be able to start automatically redis
in dev mode, or you must provide your redis address in configuration.

## Configuration

### Build time

The Quarkus application configuration is located in `src/main/resources/application.properties`.
[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

### Run time

Every configuration in application.properties can be overridden at run time by environment variables.
In general the name substitution of parameter follows simple rules. For example, foo.bar.baz should be replaced by
FOO_BAR_BAZ.
You can find complete substitution rules [here](https://quarkus.io/guides/config-reference#environment-variables).

## Packaging and running the application

This application uses JDK 17+.

### Running the application in dev mode

To quickly test your configuration you can run application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

### The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory. Be aware that it’s not an _über-jar_ as
the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

### Running app in docker

```shell script
docker build -t camara/qod  -f Dockerfile .
docker run -i --rm -p 8080:8080 camara/qod
```

## Play with swagger-ui

When application is running, you can play with [swagger-ui](http://localhost:8080/qod/v1/q/swagger-ui) to test the
service. 

