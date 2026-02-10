plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

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
    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = "cachedflow-ext-android"
                groupId = "ru.dapadz"
                version = "1.0.0"
                from(components["release"])
                artifact(androidJavadocJar.get())

                pom {
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
    }

    signing {
        val keyFilePath = providers.gradleProperty("signingKeyFile").get()
        val pass = providers.gradleProperty("signingPassword").get()
        val keyText = file(keyFilePath).readText(Charsets.UTF_8)
        useInMemoryPgpKeys(keyText, pass)
        sign(publishing.publications["maven"])
    }
}