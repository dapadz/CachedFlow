import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    `maven-publish`
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

tasks.register("printSigningProps") {
    doLast {
        val signingKey = localProperties.getProperty("signingKey")
            ?: providers.gradleProperty("signingKey").orNull
        val signingKeyFile = localProperties.getProperty("signingKeyFile")
            ?: providers.gradleProperty("signingKeyFile").orNull
        val signingPassword = localProperties.getProperty("signingPassword")
            ?: providers.gradleProperty("signingPassword").orNull

        println("signingKey present: ${!signingKey.isNullOrBlank()}")
        println("signingKeyFile present: ${!signingKeyFile.isNullOrBlank()}")
        println("signingPassword present: ${!signingPassword.isNullOrBlank()}")

        if (!signingKeyFile.isNullOrBlank()) {
            println("signingKeyFile: $signingKeyFile")
        }
    }
}
subprojects {
    plugins.withId("maven-publish") {
        publishing {
            repositories {
                maven {
                    name = "localBundle"
                    url = uri(rootProject.layout.buildDirectory.dir("local-maven"))
                }
            }
        }
    }
}
