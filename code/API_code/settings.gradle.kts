pluginManagement {
    val quarkusPlatformArtifactId: String by settings
    val quarkusVersion: String by settings

    val javaVersion: String by settings
    val openApiGeneratorVersion: String by settings
    val dependencyCheckVersion: String by settings
    val detektVersion: String by settings
    val dependencyLicenseReportVersion: String by settings
    val dependencyUpdateVersion: String by settings

    fun getKotlinVersion(): String {
        val propertyResource =
            java.net.URL(
                "${repositories.mavenCentral().url}io/quarkus/" +
                    "${quarkusPlatformArtifactId}/${quarkusVersion}/${quarkusPlatformArtifactId}-${quarkusVersion}.pom"
            )

        val dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val bom = dbf.newDocumentBuilder().parse(propertyResource.openStream())

        val xPathfactory = javax.xml.xpath.XPathFactory.newInstance()
        val xpath = xPathfactory.newXPath()

        val depXPath =
            xpath.compile("/project/dependencyManagement/dependencies/dependency[artifactId='kotlin-compiler']")
        val depend = depXPath.evaluate(bom, javax.xml.xpath.XPathConstants.NODESET) as org.w3c.dom.NodeList
        return xpath.evaluate("version", depend.item(0))
    }

    val kotlinVersion = getKotlinVersion()

    println("Gradle Version ${gradle.gradleVersion}")
    println("Kotlin Version $kotlinVersion")

    fun verifyConsistency() {
        println()
        val regexDo = Regex("openjdk-(?<jd>.+)-runtime")

        val (jd) = File("${rootDir}/Dockerfile").useLines { lines ->
            lines.map { regexDo.find(it) }
                .filterNotNull()
                .first()
                .destructured
        }

        if (jd != javaVersion) {
            val errMsg = "Dockerfile: Inconsistent Jdk Version found $jd expected $javaVersion"
            println("Consistency Check: $errMsg")
            throw GradleException(
                "Consistency Check",
                Throwable(errMsg)
            )
        }

        println("Consistency Check: Dockerfile OK\n")
    }

    verifyConsistency()

    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    plugins {
        id("io.quarkus") version quarkusVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("org.openapi.generator") version openApiGeneratorVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("org.owasp.dependencycheck") version dependencyCheckVersion
        id("io.github.thakurvijendar.dependency-license-report") version dependencyLicenseReportVersion
        id("com.github.ben-manes.versions") version dependencyUpdateVersion
    }
}

rootProject.name = "camara-qod"
