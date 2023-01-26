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
    id("io.quarkus")
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.allopen") version "1.7.21"
    id("org.openapi.generator") version "6.2.0"
    id("org.owasp.dependencycheck") version "7.4.4"
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    jacoco
    id("net.researchgate.release") version "2.8.1"
    id("io.github.thakurvijendar.dependency-license-report") version "2.1"
    idea
    id("org.sonarqube") version "3.4.0.2513"
}

val quarkusPluginVersion: String by project
val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project


repositories {
    mavenLocal()
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")
    implementation("io.quarkus:quarkus-oidc-client-reactive-filter")
    implementation("io.quarkus:quarkus-oidc-client")

    implementation("io.quarkus:quarkus-smallrye-opentracing")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-metrics")

    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-undertow")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-kubernetes-config")

    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("io.swagger:swagger-annotations:1.6.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.3")
    implementation("org.openapitools:jackson-databind-nullable:0.2.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.quarkus:quarkus-jacoco")
    testImplementation("io.rest-assured:rest-assured:5.1.1")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.33.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.inject.ApplicationScoped")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += listOf("-Xjvm-default=all")
        jvmTarget = JavaVersion.VERSION_11.toString()
        javaParameters = true
    }
    dependsOn("swagger", "swagger-scef")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<io.quarkus.gradle.tasks.QuarkusAddExtension> {
    extensionsToAdd = listOf(
        "quarkus-undertow",
        "quarkus-resteasy-reactive-jackson",
        "quarkus-rest-client-reactive-jackson",
        "quarkus-hibernate-validator",
        "quarkus-jaxb",
        "quarkus-redis"
    )
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
    inputSpec.set("$rootDir/src/main/resources/META-INF/openapi.yaml")
    outputDir.set("$buildDir/swagger")
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
            "useOneOfInterfaces" to "true"
        )
    )
}

tasks.create("swagger-scef", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/META-INF/fixed-scef-ericsson.yaml")
    outputDir.set("$buildDir/swagger-scef")
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
            "java8" to "true"
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val snapshotsRepoUrl = uri("https://artifactory.tech.orange/artifactory/maven-snapshot-repo/")
val releasesRepoUrl = uri("https://artifactory.tech.orange/artifactory/maven-release-repo/")

tasks.dependencyCheckAnalyze {
    isEnabled = true
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config = files("$projectDir/config/detekt/detekt.yml")
    ignoreFailures = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(false) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(false) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}

tasks.test {
    configure<JacocoTaskExtension> {
        excludeClassLoaders = listOf("*QuarkusClassLoader")
        destinationFile?.renameTo(layout.buildDirectory.file("jacoco-quarkus.exec").get().asFile)
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}
