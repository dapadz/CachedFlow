import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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

android {
    namespace = "com.dapadz.cachedflow.cache.android"
    compileSdk = 36

    defaultConfig { minSdk = 24 }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    api(project(":cached_flow"))
}

// Пустой javadocJar (Central часто требует)
val androidJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        publications {
            val publication = (findByName("maven") as? MavenPublication)
                ?: create<MavenPublication>("maven") {
                    from(components["release"])
                }

            publication.artifactId = "cachedflow-ext-android"
            publication.artifact(androidJavadocJar.get())
            publication.pom {
                name.set("CachedFlow Ext Android")
                description.set("Android extensions for CachedFlow.")
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
        sign(extensions.getByType(PublishingExtension::class.java).publications["maven"])
    }
}
