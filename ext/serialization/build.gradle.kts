plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    `maven-publish`
    signing
}

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
    implementation(libs.jetbrains.kotlinx.serialization)
    api(project(":cached_flow"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "cachedflow-ext-serialization"
            groupId = "ru.dapadz"
            version = "1.0.0"
            from(components["java"])

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
}

signing {
    val keyFilePath = providers.gradleProperty("signingKeyFile").get()
    val pass = providers.gradleProperty("signingPassword").get()
    val keyText = file(keyFilePath).readText(Charsets.UTF_8)
    useInMemoryPgpKeys(keyText, pass)
    sign(publishing.publications)
}