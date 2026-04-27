import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import java.util.Properties

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    `maven-publish`
    signing
}

val cachedFlowPublishGroup = providers
    .gradleProperty("cachedFlowPublishGroup")
    .orElse("ru.dapadz")

val cachedFlowPublishVersion = providers
    .gradleProperty("cachedFlowPublishVersion")
    .orElse("1.0.0")

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val signingKey = localProperties.getProperty("signingKey")
    ?: providers.gradleProperty("signingKey").orNull
val signingKeyFile = localProperties.getProperty("signingKeyFile")
    ?: providers.gradleProperty("signingKeyFile").orNull
val signingPassword = localProperties.getProperty("signingPassword")
    ?: providers.gradleProperty("signingPassword").orNull

group = cachedFlowPublishGroup.get()
version = cachedFlowPublishVersion.get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "cachedflow"
            from(components["java"])

            pom {
                name.set("CachedFlow")
                description.set("A lightweight caching utility for Kotlin Flow.")
                url.set("https://github.com/dapadz/CachedFlow")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/dapadz/CachedFlow")
                    connection.set("scm:git:https://github.com/dapadz/CachedFlow.git")
                    developerConnection.set("scm:git:ssh://git@github.com:dapadz/CachedFlow.git")
                }
                developers {
                    developer {
                        id.set("dapadz")
                        name.set("dapadz")
                    }
                }
            }
        }
    }
}

tasks.withType<Sign>().configureEach {
    doFirst {
        println("SIGN TASK: $path, signatory=" + (project.extensions.getByType(org.gradle.plugins.signing.SigningExtension::class.java).signatory))
    }
}

tasks.register<Jar>("rootSourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

afterEvaluate {
    if (signingPassword.isNullOrBlank()) {
        logger.lifecycle("Signing is skipped for ${project.path}: set signingPassword and signingKey or signingKeyFile in local.properties")
        return@afterEvaluate
    }

    val keyText = when {
        !signingKey.isNullOrBlank() -> signingKey
        !signingKeyFile.isNullOrBlank() -> {
            val keyFile = file(signingKeyFile)
            if (!keyFile.exists()) {
                logger.lifecycle("Signing is skipped for ${project.path}: signingKeyFile does not exist: $signingKeyFile")
                return@afterEvaluate
            }
            keyFile.readText(Charsets.UTF_8)
        }

        else -> {
            logger.lifecycle("Signing is skipped for ${project.path}: set signingKey or signingKeyFile in local.properties")
            return@afterEvaluate
        }
    }

    extensions.configure<SigningExtension>("signing") {
        useInMemoryPgpKeys(keyText, signingPassword)
        sign(extensions.getByType(PublishingExtension::class.java).publications["maven"])
    }
}
