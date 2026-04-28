import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip
import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
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

data class PublishableModule(
    val taskSuffix: String,
    val projectPath: String,
    val description: String
)

val publishableModules = listOf(
    PublishableModule(
        taskSuffix = "CachedFlow",
        projectPath = ":cached_flow",
        description = "the CachedFlow core library"
    ),
    PublishableModule(
        taskSuffix = "CachedFlowSerialization",
        projectPath = ":ext:serialization",
        description = "the CachedFlow kotlinx.serialization extension"
    ),
    PublishableModule(
        taskSuffix = "CachedFlowAndroid",
        projectPath = ":ext:android",
        description = "the CachedFlow Android extension"
    )
)

fun publicationTask(projectPath: String, taskName: String): String = "$projectPath:$taskName"

publishableModules.forEach { module ->
    tasks.register("publish${module.taskSuffix}") {
        group = "publishing"
        description = "Publishes ${module.description} using the module's configured Maven Publish repositories"
        dependsOn(publicationTask(module.projectPath, "publish"))
    }

    tasks.register("publish${module.taskSuffix}ToMavenLocal") {
        group = "publishing"
        description = "Publishes ${module.description} to Maven Local"
        dependsOn(publicationTask(module.projectPath, "publishToMavenLocal"))
    }

    tasks.register("publish${module.taskSuffix}ToLocalBundle") {
        group = "publishing"
        description = "Publishes ${module.description} to ${layout.buildDirectory.dir("local-maven").get().asFile}"
        dependsOn(publicationTask(module.projectPath, "publishAllPublicationsToLocalBundleRepository"))
    }
}

tasks.register("publishCachedFlowAll") {
    group = "publishing"
    description = "Publishes all releaseable CachedFlow modules using their configured Maven Publish repositories"
    dependsOn(publishableModules.map { "publish${it.taskSuffix}" })
}

tasks.register("publishCachedFlowAllToMavenLocal") {
    group = "publishing"
    description = "Publishes all releaseable CachedFlow modules to Maven Local"
    dependsOn(publishableModules.map { "publish${it.taskSuffix}ToMavenLocal" })
}

tasks.register("publishCachedFlowAllToLocalBundle") {
    group = "publishing"
    description = "Publishes all releaseable CachedFlow modules to the root local bundle repository"
    dependsOn("cleanCachedFlowPublishingDirs")
    dependsOn(publishableModules.map { "publish${it.taskSuffix}ToLocalBundle" })
}

tasks.register<Delete>("cleanCachedFlowPublishingDirs") {
    group = "publishing"
    description = "Cleans generated publishing directories used for local bundle and central bundle assembly"
    delete(
        layout.buildDirectory.dir("local-maven"),
        layout.buildDirectory.dir("central-staging"),
        layout.buildDirectory.dir("central-bundle")
    )
}

val stageCachedFlowForCentral by tasks.registering(Sync::class) {
    group = "publishing"
    description = "Stages a Maven repository layout for all CachedFlow release artifacts before zipping"
    dependsOn("publishCachedFlowAllToLocalBundle")

    from(layout.buildDirectory.dir("local-maven"))
    into(layout.buildDirectory.dir("central-staging"))

    exclude("**/maven-metadata*.xml")
    exclude("**/maven-metadata*.xml.*")
    exclude("**/.DS_Store")
    exclude("**/._*")
}

val centralBundleFile = layout.buildDirectory.file("central-bundle/cachedflow-central-bundle.zip")

val bundleCachedFlowForCentral by tasks.registering(Zip::class) {
    group = "publishing"
    description = "Creates a publishable Maven Central zip bundle from the staged CachedFlow artifacts"
    dependsOn(stageCachedFlowForCentral)

    from(layout.buildDirectory.dir("central-staging"))
    destinationDirectory.set(layout.buildDirectory.dir("central-bundle"))
    archiveFileName.set(centralBundleFile.get().asFile.name)
    includeEmptyDirs = false
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.register("publishCachedFlowMavenBundle") {
    group = "publishing"
    description = "Builds the publishable zip bundle for uploading CachedFlow artifacts to Maven Central"
    dependsOn(bundleCachedFlowForCentral)

    doLast {
        println("Maven bundle created at: ${centralBundleFile.get().asFile.absolutePath}")
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
