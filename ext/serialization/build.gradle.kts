import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    `maven-publish`
    signing
}

val cachedFlowPublishGroup = providers
    .gradleProperty("cachedFlowPublishGroup")
    .orElse("ru.dapadz")

val cachedFlowPublishVersion = providers
    .gradleProperty("cachedFlowPublishVersion")
    .orElse("1.1.0")

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

val publishedArtifactId = "cachedflow-ext-serialization"

base {
    archivesName.set("cachedflow-ext-serialization")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/java")
            dependencies {
                api(project(":cached_flow"))
                implementation(libs.jetbrains.kotlinx.serialization)
            }
        }

        commonTest {
            kotlin.srcDir("src/commonTest/kotlin")
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.property)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "ru.dapadz.cachedflow.cache.ext.serialization"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].java.setSrcDirs(emptyList<String>())
}

afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        publications.withType(MavenPublication::class.java).configureEach {
            val publicationName = name
            artifactId = when (publicationName) {
                "kotlinMultiplatform" -> publishedArtifactId
                "androidRelease" -> "$publishedArtifactId-android"
                else -> "$publishedArtifactId-${publicationName.lowercase()}"
            }

            val javadocJarTask = tasks.register("${publicationName}JavadocJar", Jar::class) {
                archiveBaseName.set("${project.name}-${publicationName.lowercase()}-javadoc")
                archiveClassifier.set("javadoc")
            }
            artifact(javadocJarTask)

            pom {
                name.set("CachedFlow Ext Serialization")
                description.set("Kotlinx Serialization extensions for CachedFlow.")
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
        sign(extensions.getByType(PublishingExtension::class.java).publications)
    }
}
