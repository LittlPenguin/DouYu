import java.io.File

fun loadDotEnv(file: File): Map<String, String> {
    if (!file.isFile) return emptyMap()

    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separatorIndex = line.indexOf('=')
            if (separatorIndex <= 0) {
                null
            } else {
                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim().trim('"')
                key to value
            }
        }
        .toMap()
}

fun ensureTrailingSlash(value: String): String =
    if (value.endsWith("/")) value else "$value/"

fun buildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

fun escapeXml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

val repoRootDir = rootProject.projectDir.parentFile
val localEnv = loadDotEnv(repoRootDir.resolve(".env"))

fun envConfig(key: String, defaultValue: String): String =
    providers.environmentVariable(key).orNull ?: localEnv[key] ?: defaultValue

val douyuBackendHost = envConfig("DOUYU_BACKEND_HOST", "10.0.2.2")
val douyuBackendPort = envConfig("DOUYU_BACKEND_PORT", "8081")
val douyuDebugApiBaseUrl = ensureTrailingSlash(
    envConfig("DOUYU_ANDROID_API_BASE_URL", "http://$douyuBackendHost:$douyuBackendPort/")
)
val douyuReleaseApiBaseUrl = ensureTrailingSlash(
    envConfig("DOUYU_ANDROID_RELEASE_API_BASE_URL", "https://api.example.invalid/")
)
val douyuDebugCleartextHosts = envConfig(
    "DOUYU_ANDROID_CLEARTEXT_HOSTS",
    listOf(douyuBackendHost, "10.0.2.2", "localhost").joinToString(",")
)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val generatedDebugEnvResDir = layout.buildDirectory.asFile.get().resolve("generated/res/douyuEnv/debug")
val generateDebugEnvResources by tasks.registering {
    inputs.property("douyuDebugCleartextHosts", douyuDebugCleartextHosts)
    outputs.dir(generatedDebugEnvResDir)

    doLast {
        val xmlDir = generatedDebugEnvResDir.resolve("xml")
        xmlDir.mkdirs()

        val domains = douyuDebugCleartextHosts
            .split(',', ';', ' ', '\n', '\r', '\t')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val domainConfigs = domains.joinToString(separator = "\n\n") { domain ->
            """
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">${escapeXml(domain)}</domain>
    </domain-config>
            """.trimEnd()
        }

        xmlDir.resolve("network_security_config.xml").writeText(
            """
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

$domainConfigs
</network-security-config>
            """.trimIndent(),
            Charsets.UTF_8
        )
    }
}

android {
    namespace = "cn.edu.app.douyu"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cn.edu.app.douyu"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", buildConfigString(douyuDebugApiBaseUrl))
        }

        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", buildConfigString(douyuReleaseApiBaseUrl))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("debug") {
            res.srcDir(generatedDebugEnvResDir)
        }
    }
}

tasks.matching { it.name == "preDebugBuild" || it.name == "mergeDebugResources" }.configureEach {
    dependsOn(generateDebugEnvResources)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.compose.animation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
