/*
* Software Name : camara-qod-api
* Version: 0.1.0
* SPDX-FileCopyrightText: Copyright (c) 2022 Orange
* SPDX-License-Identifier: Apache-2.0
*
* This software is distributed under the Apache-2.0,
* the text of which is available at https://www.apache.org/licenses/LICENSE-2.0
* or see the "LICENCE" file for more details.
*
* Author: patrice.conil@orange.com
*/


plugins {
    idea
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("org.openapi.generator")
    id("org.owasp.dependencycheck")
    id("io.gitlab.arturbosch.detekt")
    id("io.github.thakurvijendar.dependency-license-report")
    id("com.github.ben-manes.versions")
}

val quarkusPlatformArtifactId: String by project
val quarkusPlatformGroupId: String by project
val quarkusVersion: String by project
val javaVersion: String by project
val kotlinVersion: String by project
val detektVersion: String by project
val wiremockJre8Version: String by project
val mockitoKotlinVersion: String by project
val swaggerAnnotationVersion: String by project
val jacksonDatabindNullableVersion: String by project
val assertjCoreVersion: String by project

val javaEnumVersion = JavaVersion.valueOf("VERSION_$javaVersion")

repositories {
    mavenLocal()
    maven {
        setUrl("https://jcenter.bintray.com/")
    }
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusVersion}"))
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")

    implementation("io.quarkus:quarkus-arc")

    implementation("io.quarkus:quarkus-oidc-client-reactive-filter")
    implementation("io.quarkus:quarkus-oidc-client")

    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")

    implementation("jakarta.validation:jakarta.validation-api")
    implementation("io.quarkus:quarkus-hibernate-validator")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    implementation("io.quarkus:quarkus-undertow")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-kubernetes-config")

    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-elytron-security-properties-file")

    implementation("org.jboss.logmanager:log4j2-jboss-logmanager")

    implementation("org.apache.commons:commons-lang3")

    implementation("io.swagger:swagger-annotations:$swaggerAnnotationVersion")
    implementation("org.openapitools:jackson-databind-nullable:$jacksonDatabindNullableVersion")

    detekt("io.gitlab.arturbosch.detekt:detekt-cli:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:$detektVersion")


    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.quarkus:quarkus-jacoco")
    testImplementation("io.rest-assured:rest-assured")

    testImplementation("com.github.tomakehurst:wiremock-jre8:$wiremockJre8Version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.assertj:assertj-core:$assertjCoreVersion")
}

java {
    sourceCompatibility = javaEnumVersion
    targetCompatibility = javaEnumVersion
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.inject.ApplicationScoped")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = javaEnumVersion.toString()
        freeCompilerArgs += listOf("-Xjvm-default=all")
        javaParameters = true
    }
    dependsOn("swagger", "swagger-admin", "swagger-scef", "swagger-notif")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            setSrcDirs(
                listOf(
                    "$buildDir/swagger/src/gen/java",
                    "$buildDir/swagger-scef/src/gen/java"
                )
            )
        }
    }
    test {
        java {
            setSrcDirs(listOf("test"))
        }
    }
}

tasks.create("swagger", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/qod.yaml")
    outputDir.set("$buildDir/swagger")
    skipValidateSpec.set(false)
    additionalProperties.putAll(
        mapOf(
            "library" to "quarkus",
            "generateApis" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "hideGenerationTimestamp" to "true",
            "useTags" to "true",
            "useBeanValidation" to "true",
            "invokerPackage" to "com.camara.client",
            "modelPackage" to "com.camara.model",
            "apiPackage" to "com.camara.api",
            "dateLibrary" to "java8",
            "java8" to "true",
            //"useOneOfInterfaces" to "true",
            "legacyDiscriminatorBehavior" to "true",
            "useJakartaEe" to "true"
        )
    )
}

tasks.create("swagger-notif", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/notification.yaml")
    outputDir.set("$buildDir/swagger")
    skipValidateSpec.set(false)
    additionalProperties.putAll(
        mapOf(
            "library" to "quarkus",
            "generateApis" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "hideGenerationTimestamp" to "true",
            "useTags" to "true",
            "useBeanValidation" to "true",
            "invokerPackage" to "com.camara.client",
            "modelPackage" to "com.camara.model",
            "apiPackage" to "com.camara.api",
            "dateLibrary" to "java8",
            "java8" to "true",
            //"useOneOfInterfaces" to "true",
            "legacyDiscriminatorBehavior" to "true",
            "useJakartaEe" to "true"
        )
    )
}

tasks.create("swagger-scef", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/fixed-scef-ericsson.yaml")
    outputDir.set("$buildDir/swagger-scef")
    skipValidateSpec.set(false)
    additionalProperties.putAll(
        mapOf(
            "library" to "quarkus",
            "generateApis" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "hideGenerationTimestamp" to "true",
            "useTags" to "false",
            "useBeanValidation" to "true",
            "invokerPackage" to "com.camara.scef.client",
            "modelPackage" to "com.camara.scef.model",
            "apiPackage" to "com.camara.scef.api",
            "dateLibrary" to "java8",
            "java8" to "true",
            "legacyDiscriminatorBehavior" to "false",
            "useJakartaEe" to "true"//,
            //"returnResponse" to "true"
        )
    )
}

tasks.create("swagger-admin", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/admin.yaml")
    outputDir.set("$buildDir/swagger")
    skipValidateSpec.set(false)
    additionalProperties.putAll(
        mapOf(
            "library" to "quarkus",
            "generateApis" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "hideGenerationTimestamp" to "true",
            "useTags" to "true",
            "useBeanValidation" to "true",
            "invokerPackage" to "com.camara.client",
            "modelPackage" to "com.camara.model",
            "apiPackage" to "com.camara.api",
            "dateLibrary" to "java8",
            "java8" to "true",
            "useOneOfInterfaces" to "true",
            "legacyDiscriminatorBehavior" to "false",
            "useJakartaEe" to "true"
        )
    )
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    ignoreFailures = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(false) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}

