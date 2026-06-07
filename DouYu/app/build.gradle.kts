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
    compileSdk = 36

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
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")

    testImplementation(libs.junit)
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
