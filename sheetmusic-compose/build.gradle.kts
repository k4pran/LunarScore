import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    `maven-publish`
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    explicitApi()

    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    android {
        namespace = "com.ryanjames.sheetmusic.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        // Required so the bundled Bravura font is packaged for Android consumers.
        androidResources.enable = true
    }
    iosArm64()
    iosSimulatorArm64()
    // Rendering tests run in headless Chrome, so they exercise the real browser canvas and font loading.
    // `executable()` makes webpack bundle the Skiko runtime for those tests (CMP-4906); only klibs are published.
    js {
        browser { testTask { useKarma { useChromeHeadless() } } }
        binaries.executable()
    }
    wasmJs {
        browser { testTask { useKarma { useChromeHeadless() } } }
        binaries.executable()
    }

    // Fonts load synchronously everywhere except the web; `nonWebMain` holds the shared synchronous path.
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withCompilations { it.platformType != KotlinPlatformType.js && it.platformType != KotlinPlatformType.wasm }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":sheetmusic-core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    // Keep the generated Res class out of the public API.
    publicResClass = false
    packageOfResClass = "com.ryanjames.sheetmusic.compose.generated.resources"
}
