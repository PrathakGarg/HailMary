plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

import java.io.ByteArrayOutputStream

android {
    namespace = "com.arise.habitquest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.arise.habitquest"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Activity
    implementation(libs.androidx.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager
    implementation(libs.workmanager.ktx)

    // Coroutines
    implementation(libs.coroutines.android)

    // Serialization
    implementation(libs.kotlin.serialization.json)

    // Glance Widget
    implementation(libs.glance.appwidget)

    // Instrumented tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.workmanager.testing)

    // JVM unit tests
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
}

fun resolveAdbExecutable(): String {
    val sdkFromEnv = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (!sdkFromEnv.isNullOrBlank()) {
        val adbPath = file("$sdkFromEnv/platform-tools/adb")
        if (adbPath.exists()) return adbPath.absolutePath
    }
    return "adb"
}

val keepDebugData = providers.gradleProperty("keepDebugData")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

val prepareConnectedTestDevice = tasks.register("prepareConnectedTestDevice") {
    group = "verification"
    description = "Clears app/test app data on connected device before instrumentation tests."

    doLast {
        if (keepDebugData.get()) {
            println("[prepareConnectedTestDevice] keepDebugData=true -> skipping app data clear.")
            return@doLast
        }

        val adb = resolveAdbExecutable()
        val appId = "com.arise.habitquest"
        val testAppId = "$appId.test"

        fun runAdb(vararg args: String): String {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine(adb, *args)
                standardOutput = stdout
                isIgnoreExitValue = false
            }
            return stdout.toString().trim()
        }

        fun runAdbAllowFailure(vararg args: String): Pair<Int, String> {
            val stdout = ByteArrayOutputStream()
            val result = exec {
                commandLine(adb, *args)
                standardOutput = stdout
                isIgnoreExitValue = true
            }
            return result.exitValue to stdout.toString().trim()
        }

        fun connectedDevices(): List<String> {
            val (exitCode, output) = runAdbAllowFailure("devices")
            if (exitCode != 0) return emptyList()
            return output
                .lineSequence()
                .drop(1) // Header: "List of devices attached"
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split(Regex("\\s+"))
                    val serial = parts.getOrNull(0) ?: return@mapNotNull null
                    val state = parts.getOrNull(1) ?: return@mapNotNull null
                    if (state == "device") serial else null
                }
                .toList()
        }

        fun isInstalled(packageName: String): Boolean {
            val (_, output) = runAdbAllowFailure("shell", "pm", "list", "packages", packageName)
            return output.lines().any { it.trim() == "package:$packageName" }
        }

        val devices = connectedDevices()
        if (devices.isEmpty()) {
            throw org.gradle.api.GradleException(
                """
                No connected Android device/emulator detected.
                Start an emulator or connect a device, then rerun tests.

                Example emulator command:
                  ~/Android/Sdk/emulator/emulator -avd HailMary_API_34
                """.trimIndent()
            )
        }

        println("[prepareConnectedTestDevice] Connected devices: ${devices.joinToString()}")
        runAdb("wait-for-device")
        if (isInstalled(appId)) {
            println("[prepareConnectedTestDevice] Clearing data for $appId")
            runAdb("shell", "pm", "clear", appId)
        } else {
            println("[prepareConnectedTestDevice] $appId not installed yet; skipping clear.")
        }

        if (isInstalled(testAppId)) {
            println("[prepareConnectedTestDevice] Clearing data for $testAppId")
            runAdb("shell", "pm", "clear", testAppId)
        } else {
            println("[prepareConnectedTestDevice] $testAppId not installed yet; skipping clear.")
        }

        println("[prepareConnectedTestDevice] Device data reset complete.")
    }
}

tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
    dependsOn(prepareConnectedTestDevice)
}
